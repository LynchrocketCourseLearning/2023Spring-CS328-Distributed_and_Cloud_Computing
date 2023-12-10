package tests.testZK;

import RMIZK.ServiceProvider;
import myrmi.server.UnicastRemoteObject;
import tests.publicFile.Foo;
import tests.publicFile.Foo2;
import tests.publicFile.IFoo;

public class Server {
    public static void main(String[] args) throws Exception {
        ServiceProvider provider = new ServiceProvider();

        IFoo foo = new Foo(101, "Hi, I am foo from Server");
        IFoo foo2 = new Foo2("Hi, I am foo2 from Server");
        provider.publish(foo, "remoteFoo", "localhost", 1111); // port defined here is the actual port
        provider.publish(UnicastRemoteObject.exportObject(foo2, 102), "remoteFoo2", "localhost", 1112);
    }
}