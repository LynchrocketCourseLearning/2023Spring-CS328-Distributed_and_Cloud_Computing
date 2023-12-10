package tests.publicFile;

import myrmi.exception.RemoteException;
import myrmi.server.UnicastRemoteObject;

// implements IFoo but does not extend UnicastRemoteObject
public class Foo2 implements IFoo {
    String msg = " Hi from remote Foo2!";

    public Foo2() throws RemoteException {

    }
    public Foo2(String msg) throws RemoteException {
        this.msg = msg;
    }
    @Override
    public String getMessage() {
        return msg;
    }
}
