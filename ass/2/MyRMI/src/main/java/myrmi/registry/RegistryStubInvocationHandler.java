package myrmi.registry;

import myrmi.exception.AlreadyBoundException;
import myrmi.exception.NotBoundException;
import myrmi.exception.RemoteException;
import myrmi.server.RemoteObjectRef;
import myrmi.server.Util;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


public class RegistryStubInvocationHandler implements InvocationHandler {
    private RemoteObjectRef registryRef;
    private Registry registryStub;

    public RegistryStubInvocationHandler(String host, int port) {
        this.registryRef = new RemoteObjectRef(host, port, 0, "myrmi.registry.Registry");
        this.registryStub = (Registry) Util.createStub(this.registryRef);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException, AlreadyBoundException, NotBoundException, Throwable {
        Object result = null;
        if ("lookup".equals(method.getName()) || "unbind".equals(method.getName())) {
            //TODO: Here you need special handling for invoking ``lookup'' method,
            // because it returns the stub of a remote object
            try {
                result = method.invoke(this.registryStub, args);
                System.out.println("[I_C]>> RegistryStub Invoke " + method.getName());
            } catch (InvocationTargetException e) { // NotBoundException is thrown
                e.printStackTrace();
                System.out.println("[R_C]>> Such stub not found");
            }
        } else if ("bind".equals(method.getName())) {
            try {
                result = method.invoke(this.registryStub, args);
                System.out.println("[I_C]>> RegistryStub Invoke " + method.getName());
            } catch (InvocationTargetException e) { // AlreadyBoundException is thrown
                e.printStackTrace();
                System.out.println("[R_C]>> Such stub has been bound");
            }
        } else {
            try {
                result = method.invoke(this.registryStub, args);
                System.out.println("[I_C]>> RegistryStub Invoke " + method.getName());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
