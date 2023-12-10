package DockerComponents.ServerComponent;

import DockerComponents.ServerComponent.MatMul.MatMulMPI;
import DockerComponents.ServerComponent.MatMul.MatMulMPIImpl;
import myrmi.registry.LocateRegistry;
import myrmi.registry.Registry;
import myrmi.server.UnicastRemoteObject;

public class ServerComponent {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 1200;

        try {
            MatMulMPI mmm = new MatMulMPIImpl();
            MatMulMPI stub = (MatMulMPI) UnicastRemoteObject.exportObject(mmm, 0);

            Registry registry = LocateRegistry.getRegistry(host, port);
            registry.rebind("MatMul", stub);
            System.out.println("Mortgage Server is ready to listen... ");
        } catch (Exception e) {
            System.err.println("Server exception thrown: " + e.toString());
            e.printStackTrace();
        }
    }
}
