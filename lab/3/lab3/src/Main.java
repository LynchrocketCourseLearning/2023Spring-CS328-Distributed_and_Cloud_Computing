import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

    public static void main(String[] args) {

        // run with -Djava.security.manager -Djava.security.policy=/path/to/security.policy
        try {

            Registry registry = LocateRegistry.createRegistry(2000);
//            System.setSecurityManager(new java.rmi.RMISecurityManager());
            IFoo foo = new Foo();
            IFoo foo2 = new Foo2();
            registry.bind("remoteFoo", foo);
            registry.bind("remoteFoo2", foo2);
            System.out.println("RMI registry started.");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

