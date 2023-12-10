package myrmi.server;

import myrmi.Remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Util {


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
