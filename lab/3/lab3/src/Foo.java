

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Foo extends UnicastRemoteObject implements IFoo{

    private static final long serialVersionUID = -5655647669552931408L;

    protected Foo() throws RemoteException {
        super();
    }

    public String getMessage() throws RemoteException {
        return " Hi from remote Foo!";
    }

}