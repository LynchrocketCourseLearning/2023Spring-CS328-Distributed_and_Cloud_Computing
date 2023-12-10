package myrmi.registry;

import myrmi.Remote;
import myrmi.exception.AlreadyBoundException;
import myrmi.exception.NotBoundException;
import myrmi.exception.RemoteException;
import myrmi.server.Skeleton;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.InetAddress;
import java.util.*;

public class RegistryImpl implements Registry {
    private final HashMap<String, Remote> bindings = new HashMap<>(101);

    /**
     * Construct a new RegistryImpl
     * and create a skeleton on given port
     **/
    public RegistryImpl(int port)
            throws RemoteException {
        Skeleton skeleton = new Skeleton(this, "127.0.0.1", port, 0);
        skeleton.start();
    }

    public Remote lookup(String name)
            throws RemoteException, NotBoundException {
        System.out.printf("[I_S]>> RegistryImpl: lookup(%s)\n", name);
        //TODO: implement method here
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null)
                throw new NotBoundException(name);
            return obj;
        }
    }

    public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException {
        System.out.printf("[I_S]>> RegistryImpl: bind(%s)\n", name);
        //TODO: implement method here
        synchronized (bindings) {
            Remote curr = bindings.get(name);
            if (curr != null)
                throw new AlreadyBoundException(name);
            bindings.put(name, obj);
        }
    }

    public void unbind(String name)
            throws RemoteException, NotBoundException {
        System.out.printf("[I_S]>> RegistryImpl: unbind(%s)\n", name);
        //TODO: implement method here
        synchronized (bindings) {
            Remote obj = bindings.get(name);
            if (obj == null)
                throw new NotBoundException(name);
            bindings.remove(name);
        }
    }

    public void rebind(String name, Remote obj)
            throws RemoteException {
        System.out.printf("[I_S]>> RegistryImpl: rebind(%s)\n", name);
        //TODO: implement method here
        bindings.put(name, obj);
    }

    public String[] list()
            throws RemoteException {
        System.out.println("[I_S]>> RegistryImpl: list()");
        //TODO: implement method here
        String[] names;
        synchronized (bindings) {
            int i = bindings.size();
            names = new String[i];
            Iterator<String> iter = bindings.keySet().iterator();
            while ((--i) >= 0)
                names[i] = iter.next();
        }
        return names;
    }

    public static void main(String[] args) {
        final int regPort = (args.length >= 1) ? Integer.parseInt(args[0])
                : Registry.REGISTRY_PORT;
        RegistryImpl registry;
        try {
            registry = new RegistryImpl(regPort);
        } catch (RemoteException e) {
            System.exit(1);
        }

        System.out.printf("RMI Registry is listening on port %d\n", regPort);
    }
}
