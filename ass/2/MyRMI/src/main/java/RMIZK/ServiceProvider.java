package RMIZK;

import myrmi.Remote;
import myrmi.exception.RemoteException;
import myrmi.registry.LocateRegistry;
import myrmi.registry.Naming;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 * RMI service provider
 */
public class ServiceProvider {
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * publish RMI service and register the RMI address onto ZooKeeper
     */
    public void publish(Remote remote, String serviceName, String host, int port) {
        String url = publishService(remote, serviceName, host, port);
        if (url != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                createNode(zk, url, host);
            }
        }
    }

    /**
     * publish RMI service
     */
    private String publishService(Remote remote, String serviceName, String host, int port) {
        String url = null;
        try {
            url = String.format("rmi://%s:%d/%s", host, port, serviceName);
            LocateRegistry.createRegistry(port);
            Naming.rebind(url, remote);
            System.out.printf("[_S]>> publish rmi service (url: %s)\n", url);
        } catch (RemoteException | URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * connect to ZooKeeper server
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            // Watcher here is to guarantee it successfully connects to ZooKeeper Server
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown(); // wake up thread
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }

    private void createNode(ZooKeeper zk, String url, String host) {
        try {
            byte[] data = (host + " " + url).getBytes();
            String path = zk.create(Constant.ZK_PROVIDER_PATH, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.printf("[_S]>> create zookeeper node (%s => %s)\n", path, url);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}