package RMIZK;

import myrmi.Remote;
import myrmi.exception.NotBoundException;
import myrmi.exception.RemoteException;
import myrmi.registry.Naming;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * RMI service consumer
 */
public class ServiceConsumer {
    private final CountDownLatch latch = new CountDownLatch(1);
    private static final Map<String, ServerService> serverMap = new HashMap<>(); // serverHost : serverService
    private static final Map<String, String> serviceMap = new HashMap<>(); // service : serverHost

    private final ExecutorService executorService = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    private final List<String> conn = new ArrayList<>();

    public ServiceConsumer() {
        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    public void close() {
        for (String serverHost : conn) {
            serverMap.get(serverHost).busyNum--;
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            // if the child nodes under /registry have changed, then call back this method
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zk);
                }
            });

            for (String node : nodeList) {
                String path = Constant.ZK_REGISTRY_PATH + "/" + node;

                byte[] dataByte = zk.getData(path, false, null);
                String[] split = new String(dataByte).split(" ");

                String serverHost = split[0];
                String url = split[1];

                // listen to every child node,
                //      if deleted, which means the server quits (no choice for unpublishing service),
                //          then remove the server and its services
                zk.exists(path, event -> {
                    if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                        ServerService serverService = serverMap.remove(serverHost);
                        for (String service : serverService.services) {
                            serviceMap.remove(service);
                        }
                    }
                });

                ServerService serverService;
                if (!serverMap.containsKey(serverHost)) { // new coming server
                    serverService = new ServerService(serverHost);
                    serverService.services.add(url);
                    serverMap.put(serverHost, serverService);
                } else { // existed server, but new coming service (in url)
                    serverService = serverMap.get(serverHost);
                    serverService.services.add(url);
                }
                serviceMap.put(url, serverHost);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * not specify the serviceName, then randomly choose a service for consumer
     */
    public <T extends Remote> T lookup()
            throws NotBoundException, ExecutionException, InterruptedException {
        T service = null;
        int size = serverMap.size();
        if (size > 0) {
            // randomly choose service
            List<String> serviceList = new ArrayList<>(serviceMap.keySet());
            int idx = ThreadLocalRandom.current().nextInt(serviceList.size());
            String url = serviceList.get(idx);

            System.out.printf("[_C]>> using url: %s\n", url);
            service = lookupService(url);

            if (service != null) {
                String serverHost = serviceMap.get(url);
                serverMap.get(serverHost).busyNum++;
                conn.add(serverHost);
//                // monitor the service
//                MonitorService<Remote> monitor = new MonitorService<>(service, serverHost);
//                Future<String> futureResult = executorService.submit(monitor);
//                if (futureResult.isDone()) {
//                    serverHost = futureResult.get();
//                    serverMap.get(serverHost).busyNum--;
//                }
            }
        }
        // 1. no server
        // 2. has server but did not find any service
        if (service == null)
            throw new NotBoundException();
        return service;
    }

    /**
     * specify the name of service, mode of look up
     */
    public <T extends Remote> T lookup(String serviceName, int mode)
            throws NotBoundException, ExecutionException, InterruptedException {
        return (mode == Constant.RANDOM_MODE) ? lookupRandom(serviceName) : lookupLeast(serviceName);
    }

    /**
     * specify the name of service, randomly choose
     */
    private <T extends Remote> T lookupRandom(String serviceName)
            throws NotBoundException, ExecutionException, InterruptedException {
        T service = null;
        int size = serverMap.size();
        if (size > 0) {
            // get all the services that satisfy the serviceName
            List<String> serviceList = new ArrayList<>(serviceMap.keySet()).stream()
                    .filter(x -> x.endsWith(serviceName))
                    .collect(Collectors.toList());
            if (serviceList.isEmpty()) // if no such service, then throw
                throw new NotBoundException();

            // randomly choose
            int idx = ThreadLocalRandom.current().nextInt(serviceList.size());
            String url = serviceList.get(idx);
            System.out.printf("[_C]>> using url: %s\n", url);
            service = lookupService(url);

            // do not forget to add busyNum
            if (service != null) {
                String serverHost = serviceMap.get(url);
                serverMap.get(serverHost).busyNum++;
                conn.add(serverHost);
            }
        }
        // 1. no server
        // 2. has server but did not find any service
        if (service == null)
            throw new NotBoundException();
        return service;
    }

    /**
     * specify the name of service, the least connection
     */
    private <T extends Remote> T lookupLeast(String serviceName)
            throws NotBoundException, ExecutionException, InterruptedException {
        T service = null;
        int size = serverMap.size();
        if (size > 0) {
            // get all the services that satisfy the serviceName
            List<String> serviceList = new ArrayList<>(serviceMap.keySet()).stream()
                    .filter(x -> x.endsWith(serviceName))
                    .collect(Collectors.toList());
            if (serviceList.isEmpty()) // if no such service, then throw
                throw new NotBoundException();

            // get the corresponding server
            List<String> serverList = serviceList.stream()
                    .map(serviceMap::get)
                    .collect(Collectors.toList());
            // find the server with the least connections
            size = serverList.size();
            int minBusyNum = Integer.MAX_VALUE;
            String url = "";
            for (int i = 0; i < size; i++) {
                ServerService serverService = serverMap.get(serverList.get(i));
                if (minBusyNum > serverService.busyNum) {
                    minBusyNum = serverService.busyNum;
                    url = serviceList.get(i);
                }
            }

            System.out.printf("[_C]>> using url: %s\n", url);
            service = lookupService(url);

            // do not forget to add busyNum
            if (service != null) {
                String serverHost = serviceMap.get(url);
                serverMap.get(serverHost).busyNum++;
                conn.add(serverHost);
            }
        }
        // 1. no server
        // 2. has server but did not find any service
        if (service == null)
            throw new NotBoundException();
        return service;
    }

    /**
     * specify the url of service
     */
    @SuppressWarnings("unchecked")
    private <T> T lookupService(String url) {
        if (url == null) return null;
        T remote = null;
        try {
            remote = (T) Naming.lookup(url);
        } catch (NotBoundException | RemoteException | URISyntaxException e) {
            e.printStackTrace();
        }
        return remote;
    }

    private static class ServerService {
        String serverHost;
        List<String> services;
        int busyNum;

        ServerService(String serverHost) {
            this.serverHost = serverHost;
            this.services = new ArrayList<>();
            this.busyNum = 0;
        }
    }
}