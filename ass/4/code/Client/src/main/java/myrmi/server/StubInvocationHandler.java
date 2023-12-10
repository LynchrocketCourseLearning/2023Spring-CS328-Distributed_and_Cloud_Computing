package myrmi.server;

import myrmi.exception.RemoteException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class StubInvocationHandler implements InvocationHandler, Serializable {
    private String host;
    private int port;
    private int objectKey;

    public StubInvocationHandler(String host, int port, int objectKey) {
        this.host = host;
        this.port = port;
        this.objectKey = objectKey;
        System.out.printf("Stub created to %s:%d, object key = %d\n", host, port, objectKey);
    }

    public StubInvocationHandler(RemoteObjectRef ref) {
        this(ref.getHost(), ref.getPort(), ref.getObjectKey());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException, IOException, ClassNotFoundException, Throwable {
//        Socket socket = new Socket(Util.defaultAccessingHost, port);
        Socket socket = new Socket();
        System.out.printf("connect to %s:%d\n", host, port);
        socket.connect(new InetSocketAddress(Util.defaultAccessingHost, port));
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

        output.writeInt(objectKey);
        output.writeUTF(method.getName());
        output.writeObject(method.getParameterTypes());
        output.writeObject(args);
        System.out.println(">>> StubInvocationHanlder invoking: "+method.getName());
        int retCode = input.readInt();
        System.out.println("<<< Finished, code: "+retCode);
        if (retCode == 1) {
            //void method
            return null;
        }
        if (retCode == -1) {
            //error in remote invocation
            throw new RemoteException();
        }
        Object result = input.readObject();

        if (retCode == 2) {
            return result;
        }

        if (retCode == 0) {
            // exception thrown
            throw (Throwable) result;
        }

        return null;
    }

}
