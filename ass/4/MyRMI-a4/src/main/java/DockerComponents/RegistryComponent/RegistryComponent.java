package DockerComponents.RegistryComponent;

import myrmi.registry.LocateRegistry;
import myrmi.registry.Registry;

public class RegistryComponent {
    public static void main(String[] args) {
        try {
            String host = "0.0.0.0";
            int port = 1200;
            Registry registry = LocateRegistry.createRegistry(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("RMI Registry is running...");
    }
}
