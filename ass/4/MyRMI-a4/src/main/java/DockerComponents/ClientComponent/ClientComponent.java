package DockerComponents.ClientComponent;

import DockerComponents.ServerComponent.MatMul.MatMulMPI;
import myrmi.registry.LocateRegistry;
import myrmi.registry.Registry;

import java.io.FileWriter;
import java.io.PrintWriter;

public class ClientComponent {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 1200;

        MatMulMPI stub = null;
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);

            stub = (MatMulMPI) registry.lookup("MatMul");
            System.out.println("Mortgage Server is ready to listen... ");
        } catch (Exception e) {
            System.err.println("Client exception thrown: " + e.toString());
            e.printStackTrace();
        }

        if (stub != null) {
            double[][] res;
            try {
                res = stub.calculate();
                PrintWriter pw = new PrintWriter(new FileWriter("result.txt"));
                int n = res.length, m = res[0].length;
                for (int i = 0; i < n; ++i) {
                    for (int j = 0; j < m; ++j) {
                        pw.printf("%.2f, ", res[i][j]);
                    }
                    pw.print(";\n");
                }
                pw.close();
            } catch (Exception e) {
                System.out.println("Remote method exception thrown: " + e.getMessage());
            }
            System.out.println("Calculation finished! Result can be found in the ./result.txt");
        } else {
            System.out.println("Fail to retrieve stub.");
        }
    }
}
