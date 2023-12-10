package DockerComponents.ServerComponent.MatMul;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MatMulMPIImpl implements MatMulMPI {
    @Override
    public double[][] calculate() {
        int n = 500;
        double[][] res = new double[n][n];
        try {
            String[] cmd = {"mpirun", "--oversubscribe", "-np", "4", "./mat_mul"};
            // String[] cmd = {"pwd"};
            ProcessBuilder pb = new ProcessBuilder(cmd);

            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String timeInfo = reader.readLine();
            String isCorrect = reader.readLine();
            System.out.println(timeInfo);
            System.out.println(isCorrect);

            String line;
            int cnt = 0;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                for (int i = 0; i < n; i++) {
                    res[cnt][i] = Double.parseDouble(row[i]);
                }
                cnt++;
            }

            int exitCode = p.waitFor();
            System.out.println("Exited with error code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }
}
