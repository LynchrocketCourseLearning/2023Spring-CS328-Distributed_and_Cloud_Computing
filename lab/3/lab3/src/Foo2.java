

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Foo2 extends UnicastRemoteObject implements IFoo {

    protected Foo2() throws RemoteException {
        super();
    }

    @Override
    public String getMessage() throws RemoteException {
        return " Hi from remote Foo2!";
    }
}
