package tests.testMyRMI;

import myrmi.registry.LocateRegistry;
import myrmi.registry.Naming;
import myrmi.registry.Registry;
import myrmi.server.UnicastRemoteObject;
import tests.publicFile.Foo;
import tests.publicFile.Foo2;
import tests.publicFile.IFoo;

public class Server {
    public static void main(String[] args) {
        try {
            IFoo foo = new Foo(100);
            IFoo foo2 = new Foo2();
            // create a registry on the port 2000
            Registry registry = LocateRegistry.createRegistry(2000);
            // bind the remote object on the registry with corresponding name
            registry.bind("remoteFoo", foo);
            registry.bind("remoteFoo2", UnicastRemoteObject.exportObject(foo2, 200));
            // the above is equivalent to the following
//            Registry registry = LocateRegistry.createRegistry(2000);
//            Naming.bind("rmi://localhost:2000/remoteFoo", foo);
//            Naming.bind("rmi://localhost:2000/remoteFoo2", UnicastRemoteObject.exportObject(foo2, 200));
            System.out.println("RMI registry started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
