package myrmi.server;

import myrmi.Remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class SkeletonReqHandler extends Thread {
    private Socket socket;
    private Remote obj;
    private int objectKey;

    public SkeletonReqHandler(Socket socket, Remote remoteObj, int objectKey) {
        this.socket = socket;
        this.obj = remoteObj;
        this.objectKey = objectKey;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            int objectKey;
            String methodName;
            Class<?>[] argTypes;
            Object[] args;
            Object result;

            // object key
            objectKey = in.readInt();
            // method name
            methodName = in.readUTF();
            try {
                // argument types
                argTypes = (Class<?>[]) in.readObject();
                // args
                args = (Object[]) in.readObject();
            } catch (ClassNotFoundException e) {
                out.writeInt(-1);
                System.err.println("Error in invocation:");
                e.printStackTrace();
                return;
            }

            try {
                Class<? extends Remote> c = this.obj.getClass();
                if (objectKey != 0 && objectKey != this.objectKey) {
                    System.err.println("Object key mismatch, get " + objectKey + ", expect: " + this.objectKey);
                    out.writeInt(-1);
                } else {
                    // get result
                    Method m = c.getMethod(methodName, argTypes);
                    if (m.getReturnType().equals(Void.TYPE)) {
                        // return type is void, just return the status code
                        m.invoke(this.obj, args);
                        System.out.println("Skeleton: Invoke void method " + methodName + " success");
                        out.writeInt(1);


                    } else {

                        result = m.invoke(this.obj, args);
                        System.out.println("Skeleton: Invoke non-void method " + methodName + " success");
                        out.writeInt(2);
                        out.writeObject(result);
                    }
                }


            } catch (InvocationTargetException e) {
                // if exception thrown, return the exception
                System.out.printf("Exception thrown in invocation, %s, %s\n", methodName, e.getCause().toString());
                result = e.getCause();
                out.writeInt(0);
                out.writeObject(result);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                out.writeInt(-1);
                System.err.println("Error in invocation:");
                e.printStackTrace();

            }
            out.flush();

        } catch (IOException e) {
            System.err.println("Error handling skeleton request:");
            e.printStackTrace();
        }
    }
}
