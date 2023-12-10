package DockerComponents.ServerComponent.MatMul;

import myrmi.Remote;

public interface MatMulMPI extends Remote {
    double[][] calculate();
}
