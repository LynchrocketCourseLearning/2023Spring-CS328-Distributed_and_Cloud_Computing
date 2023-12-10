package RMIZK;

public interface Constant {
    String ZK_CONNECTION_STRING = "localhost:2181";
    int ZK_SESSION_TIMEOUT = 5000;
    String ZK_REGISTRY_PATH = "/registry";
    String ZK_PROVIDER_PATH = ZK_REGISTRY_PATH + "/provider";
    // mode of lookup
    int RANDOM_MODE = 0;
    int LEAST_CONN_MODE = 1;
}