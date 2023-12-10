package myrmi.server;

import myrmi.info.RequestInfo;
import myrmi.info.ResultInfo;
import myrmi.exception.RemoteException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;

/**
 * deprecated, using Java API to serialize
 * */
@Deprecated
public class StubInvocationHandler implements InvocationHandler, Serializable {
    private String host;
    private int port;
    private int objectKey;

    public StubInvocationHandler(String host, int port, int objectKey) {
        this.host = host;
        this.port = port;
        this.objectKey = objectKey;
        System.out.printf("[C]>> Stub created to %s:%d, object key = %d\n", host, port, objectKey);
    }

    public StubInvocationHandler(RemoteObjectRef ref) {
        this(ref.getHost(), ref.getPort(), ref.getObjectKey());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException, IOException, ClassNotFoundException, Throwable {
        Object result = null;
        Exception exception = null;
        int status;
        int objKey;
        /*TODO: implement stub proxy invocation handler here
         *  You need to do:
         * 1. connect to remote skeleton, send method and arguments
         * 2. get result back and return to caller transparently
         * */
        System.out.printf("[I_C]>> StubInvocationHandler invokes method: %s; on the port: %d\n", method.getName(), port);
        try (Socket client = new Socket(host, port)) {
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            Class<?>[] argTypes = (args == null) ? null : Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            outputStream.writeObject(new RequestInfo(method.getName(), args, argTypes, objectKey));
            outputStream.flush();

            ObjectInputStream inputStream = new ObjectInputStream(client.getInputStream());
            Object resultObj = inputStream.readObject();
            if (resultObj instanceof ResultInfo) {
                result = ((ResultInfo) resultObj).getResult();
                exception = ((ResultInfo) resultObj).getException();
                status = ((ResultInfo) resultObj).getStatus();
                objKey = ((ResultInfo) resultObj).getObjKey();
                if (objKey != this.objectKey) throw new Exception("Invalid ObjectKey");
                if (status == -1) {
                    System.out.println("[R_C]>> Invocation Error");
                } else if (status == 0) {
                    throw exception;
                } else if (status == 1) {
                    System.out.println("[R_C]>> Success");
                }
            } else {
                throw new Exception("Unknown Error");
            }
        } finally {

        }
        return result;
    }

}
