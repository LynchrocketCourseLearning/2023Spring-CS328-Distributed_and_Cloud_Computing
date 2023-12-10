package myrmi.server;

import myrmi.Remote;
import myrmi.exception.RemoteException;


public class UnicastRemoteObject implements Remote, java.io.Serializable {
    int port;

    protected UnicastRemoteObject() throws RemoteException {
        this(0);
    }

    protected UnicastRemoteObject(int port) throws RemoteException {
        this.port = port;
        exportObject(this, port);
    }

    public static Remote exportObject(Remote obj) throws RemoteException {
        return exportObject(obj, 0);
    }

    public static Remote exportObject(Remote obj, int port) throws RemoteException {
        return exportObject(obj, Util.defaultBindingHost, port);
    }

    /**
     * create skeleton on given host:port
     * returns a stub
     **/
    public static Remote exportObject(Remote obj, String host, int port) throws RemoteException {
        String interfaceName = obj.getClass().getInterfaces()[0].getName();
        int objectKey = obj.hashCode();

        RemoteObjectRef ref = new RemoteObjectRef(host, port, objectKey, interfaceName);
        Skeleton skeleton = new Skeleton(obj, ref);
        skeleton.start();
        if (port == 0) {
            // busy waiting here until port is allocated
            while (skeleton.getPort() == 0) ;
            ref.setPort(skeleton.getPort());
        }
        return Util.createStub(ref);
    }
}
