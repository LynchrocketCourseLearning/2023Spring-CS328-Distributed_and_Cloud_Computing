package demo;

import myrmi.exception.RemoteException;
import myrmi.registry.LocateRegistry;

public class Registry {
    public static void main(String args[]) {
        try {
            LocateRegistry.createRegistry();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.print("RMI Registry is running...");

    }
}
