
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) throws Exception{

        Registry r = LocateRegistry.getRegistry("127.0.0.1", 2000);

        IFoo foo = (IFoo) r.lookup("remoteFoo");
        IFoo foo2 = (IFoo) r.lookup("remoteFoo2");
        System.out.println(foo instanceof Foo);
        System.out.println(foo.getMessage());
        System.out.println(foo2.getMessage());
    }

}
