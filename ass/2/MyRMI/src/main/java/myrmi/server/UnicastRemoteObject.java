package myrmi.server;

import myrmi.Remote;
import myrmi.exception.RemoteException;

import java.io.Serializable;

public class UnicastRemoteObject implements Remote, Serializable {
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
        return exportObject(obj, "127.0.0.1", port);
    }

    /**
     * 1. create a skeleton of the given object ``obj'' and bind with the address ``host:port''
     * 2. return a stub of the object ( Util.createStub() )
     **/
    public static Remote exportObject(Remote obj, String host, int port) throws RemoteException {
        //TODO: finish here
        int objectKey = obj.hashCode();

        Class<?>[] objInterfaces = obj.getClass().getInterfaces();
        String interfaceName = (objInterfaces.length > 0) ? objInterfaces[0].getName() : "myrmi.Remote";

        Skeleton skeleton = new Skeleton(obj, host, port, objectKey);
        skeleton.start();

        RemoteObjectRef objectRef = new RemoteObjectRef(host, skeleton.getPort(), objectKey, interfaceName);
        return Util.createStub(objectRef);
    }
}
