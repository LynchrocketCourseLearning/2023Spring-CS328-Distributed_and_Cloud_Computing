package myrmi.server;

import myrmi.Remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Skeleton extends Thread {
    static final int BACKLOG = 5;
    private Remote remoteObj;
    private String host;
    private volatile int port;
    private int objectKey;

    public int getPort() {
        return port;
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
        try {
            ServerSocket serverSoc = new ServerSocket(this.port, BACKLOG, InetAddress.getByName(this.host));
            this.port = serverSoc.getLocalPort();
            System.out.printf("Skeleton created on port %d\n", this.port);
            while (true) {
                try {
                    Socket socket = serverSoc.accept();
                    //System.out.println("New invocation received on port "+this.port);
                    new SkeletonReqHandler(socket, this.remoteObj, this.objectKey).start();
                } catch (SocketException ignored) {
                    // ignore connection reset by other side
                }
            }
        } catch (IOException e) {
            System.err.printf("Failed to create skeleton at port %d\n", this.port);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
