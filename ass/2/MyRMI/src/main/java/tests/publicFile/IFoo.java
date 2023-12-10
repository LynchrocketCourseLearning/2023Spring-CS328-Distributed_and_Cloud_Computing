package tests.publicFile;


import myrmi.Remote;
import myrmi.exception.RemoteException;

public interface IFoo extends Remote { // must extend Remote!!
    String getMessage() throws RemoteException;
}