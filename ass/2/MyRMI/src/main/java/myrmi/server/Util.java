package myrmi.server;

import myrmi.Remote;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Util {
    public static Remote createStub(RemoteObjectRef ref) {
        //TODO: finish here, instantiate an StubInvocationHandler for ref and then return a stub
        Class<?> objInterface;
        try {
            objInterface = Class.forName(ref.getInterfaceName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            objInterface = Remote.class;
        }
//        return (Remote) Proxy.newProxyInstance(objInterface.getClassLoader(), new Class[]{objInterface}, new StubInvocationHandler(ref));
        return (Remote) Proxy.newProxyInstance(objInterface.getClassLoader(), new Class[]{objInterface}, new StubInvocationProtoHandler(ref));
    }
}
