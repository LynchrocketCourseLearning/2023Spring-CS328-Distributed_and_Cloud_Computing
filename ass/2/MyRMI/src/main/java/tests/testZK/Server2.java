package tests.testZK;

import RMIZK.ServiceProvider;
import myrmi.server.UnicastRemoteObject;
import tests.publicFile.Foo;
import tests.publicFile.Foo2;
import tests.publicFile.IFoo;

public class Server2 {
    public static void main(String[] args) throws Exception {
        ServiceProvider provider = new ServiceProvider();

        IFoo foo = new Foo(201, "Hi, I am foo from Server2");
        IFoo foo2 = new Foo2("Hi, I am foo2 from Server2");
        provider.publish(foo, "remoteFoo", "localhost", 2211);
        provider.publish(UnicastRemoteObject.exportObject(foo2, 202), "remoteFoo2", "localhost", 2212);
    }
}