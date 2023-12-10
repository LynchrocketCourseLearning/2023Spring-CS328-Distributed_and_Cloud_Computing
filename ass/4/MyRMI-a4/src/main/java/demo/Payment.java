package demo;

import myrmi.Remote;
import myrmi.exception.RemoteException;

public interface Payment extends Remote {
    public double calculatePayment(double principal, double annualRate, int terms)
                    throws RemoteException;
}

