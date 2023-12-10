package myrmi.server;

import myrmi.Remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Util {
    public static final String defaultBindingHost = "0.0.0.0";
    public static final String defaultAccessingHost = "host.docker.internal";
//    public static final String defaultAccessingHost = "localhost";
    public static final int defaultBindingPort = 1200;

    public static Remote createStub(RemoteObjectRef ref) {
        try {
            Class<?> remoteInterface = Class.forName(ref.getInterfaceName());
            InvocationHandler handler = new StubInvocationHandler(ref);
            return (Remote) Proxy.newProxyInstance(remoteInterface.getClassLoader(), new Class<?>[]{remoteInterface}, handler);
        } catch (ClassNotFoundException e) {
            System.err.printf("Error creating stub for interface %s at %s:%d, class not found\n", ref.getInterfaceName(), ref.getHost(), ref.getPort());
            System.exit(1);
        }
        // shouldn't get there
        return null;
    }


}
