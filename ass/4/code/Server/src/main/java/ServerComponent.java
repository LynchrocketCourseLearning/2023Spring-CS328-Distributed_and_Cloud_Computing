import myrmi.registry.LocateRegistry;
import myrmi.registry.Registry;
import myrmi.server.UnicastRemoteObject;
import myrmi.server.Util;

public class ServerComponent {
    public static void main(String[] args) {
        String host = Util.defaultAccessingHost;
        int port = Util.defaultBindingPort;

        try {
            MatMulMPI mmm = new MatMulMPIImpl();
            MatMulMPI stub = (MatMulMPI) UnicastRemoteObject.exportObject(mmm, 43801);

            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind("MatMul", stub);
            System.out.println("Mortgage Server is ready to listen... ");
        } catch (Exception e) {
            System.err.println("Server exception thrown: " + e.toString());
            e.printStackTrace();
        }
    }
}
