package myrmi.server;

import myrmi.Remote;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Skeleton extends Thread {
    static final int BACKLOG = 5;
    private Remote remoteObj;
    private String host;
    private int port;
    private int objectKey;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Skeleton(Remote remoteObj, RemoteObjectRef ref) {
        this(remoteObj, ref.getHost(), ref.getPort(), ref.getObjectKey());
    }

    public Skeleton(Remote remoteObj, String host, int port, int objectKey) {
        super();
        this.remoteObj = remoteObj;
        this.host = host;
        this.port = port;
        this.objectKey = objectKey;
        this.setDaemon(false);
    }

    @Override
    public void run() {
        /*TODO: implement method here
         * You need to:
         * 1. create a server socket to listen for incoming connections
         * 2. use a handler thread to process each request (use SkeletonReqHandler)
         *  */
        // Thread pool is created
        ExecutorService executorService = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
        ServerSocket serverSocket = null;
        try {
            InetAddress address = InetAddress.getByName(host);
            serverSocket = new ServerSocket(port, BACKLOG, address);
            this.port = serverSocket.getLocalPort();
            while (true) {
                // blocked until accepting the incoming connection
                Socket client = serverSocket.accept();
                System.out.printf("[R_S]>> Request from port: %d\n", client.getPort());
//                Thread clientHandler = new SkeletonReqHandler(client, remoteObj, objectKey);
//                clientHandler.start();
//                Runnable clientRequest = new RequestHandler(client, remoteObj, objectKey);
                // submits a Runnable task (request handler) for execution
                Runnable clientRequest = new RequestProtoHandler(client, remoteObj, objectKey);
                executorService.submit(clientRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("[C_S]>> Server socket closed");
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
