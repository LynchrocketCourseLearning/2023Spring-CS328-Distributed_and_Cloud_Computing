package tests.publicFile;

import myrmi.exception.RemoteException;
import myrmi.server.UnicastRemoteObject;

// implements IFoo and extends UnicastRemoteObject
public class Foo extends UnicastRemoteObject implements IFoo {
    String msg = " Hi from remote Foo!";

    public Foo() throws RemoteException {
        super();
    }
    public Foo(int port) throws RemoteException {
        super(port);
    }
    public Foo(String msg) throws RemoteException {
        super();
        this.msg = msg;
    }
    public Foo(int port, String msg) throws RemoteException {
        super(port);
        this.msg = msg;
    }
    @Override
    public String getMessage() {
        return msg;
    }
}