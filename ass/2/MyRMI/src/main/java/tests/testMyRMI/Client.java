package tests.testMyRMI;

import myrmi.registry.LocateRegistry;
import myrmi.registry.Naming;
import myrmi.registry.Registry;
import tests.publicFile.IFoo;

public class Client {
    public static void main(String[] args) throws Exception {
        // get the registry, which must be registered ahead by server or someone else
        Registry r = LocateRegistry.getRegistry("127.0.0.1", 2000);
        // look up for remote object
        IFoo foo = (IFoo) r.lookup("remoteFoo");
        IFoo foo2 = (IFoo) r.lookup("remoteFoo2");
        // the above is equivalent to the following
//        IFoo foo = (IFoo) Naming.lookup("rmi://localhost:2000/remoteFoo");
//        IFoo foo2 = (IFoo) Naming.lookup("rmi://localhost:2000/remoteFoo2");
        // invoke remote method transparently
        System.out.println(foo.getMessage());
        System.out.println(foo2.getMessage());
    }
}
