import myrmi.registry.LocateRegistry;
import myrmi.registry.Registry;
import myrmi.server.Util;

public class RegistryComponent {
    public static void main(String[] args) {
        try {
            String host = Util.defaultBindingHost;
            int port = Util.defaultBindingPort;
            Registry registry = LocateRegistry.createRegistry(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("RMI Registry is running...");
    }
}
