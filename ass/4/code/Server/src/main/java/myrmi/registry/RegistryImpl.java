package myrmi.registry;

import myrmi.Remote;
import myrmi.exception.AlreadyBoundException;
import myrmi.exception.NotBoundException;
import myrmi.exception.RemoteException;
import myrmi.server.Skeleton;
import myrmi.server.Util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

public class RegistryImpl implements Registry {
    private final HashMap<String, Remote> bindings = new HashMap<>();

    /**
     * Construct a new RegistryImpl
     * and create a skeleton on given port
     **/
    public RegistryImpl(int port) throws RemoteException {
        Skeleton skeleton = new Skeleton(this, Util.defaultBindingHost, port, 0);
        skeleton.start();
    }

    public RegistryImpl(String host, int port) throws RemoteException {
        Skeleton skeleton = new Skeleton(this, host, port, 0);
        skeleton.start();
    }

    public Remote lookup(String name) throws RemoteException, NotBoundException {
        System.out.printf("RegistryImpl: lookup(%s)\n", name);
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null) {
                throw new NotBoundException(name);
            }
            return obj;
        }
    }

    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException {
        System.out.printf("RegistryImpl: bind(%s)\n", name);
        synchronized (bindings) {
            Remote curr = bindings.get(name);
            if (curr != null)
                throw new AlreadyBoundException(name);
            bindings.put(name, obj);
        }
    }

    public void unbind(String name) throws RemoteException, NotBoundException {
        System.out.printf("RegistryImpl: unbind(%s)\n", name);
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null) {
                throw new NotBoundException(name);
            }
            bindings.remove(name);
        }
    }

    public void rebind(String name, Remote obj) throws RemoteException {
        System.out.printf("RegistryImpl: rebind(%s)\n", name);
        bindings.put(name, obj);
    }

    public String[] list() throws RemoteException {
        System.out.print("RegistryImpl: list()\n");
        Set<String> keys = bindings.keySet();
        return keys.toArray(new String[keys.size()]);
    }

//    public static void main(String args[]) {
//        final int regPort = (args.length >= 1) ? Integer.parseInt(args[0])
//                : Registry.REGISTRY_PORT;
//        RegistryImpl registry;
//        try {
//            registry = new RegistryImpl(regPort);
//        } catch (RemoteException e) {
//            System.exit(1);
//        }
//
//        System.out.printf("RMI Registry is listening on port %d\n", regPort);
//
//    }
}
