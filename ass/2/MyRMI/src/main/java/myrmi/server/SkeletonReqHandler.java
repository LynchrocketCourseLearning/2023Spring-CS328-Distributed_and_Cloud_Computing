package myrmi.server;

import myrmi.info.RequestInfo;
import myrmi.info.ResultInfo;
import myrmi.Remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

@Deprecated
// Thread, deprecated
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
        int objectKey;
        String methodName = "";
        Class<?>[] argTypes;
        Object[] args;
        Object result;

        /*TODO: implement method here
         * You need to:
         * 1. handle requests from stub, receive invocation arguments, deserialization
         * 2. get result by calling the real object, and handle different cases (non-void method, void method, method throws exception, exception in invocation process)
         * Hint: you can use an int to represent the cases: -1 invocation error, 0 exception thrown, 1 void method, 2 non-void method
         *  */
        System.out.printf("[R_S]>> SkeletonReqHandler get request from socket port: %d\n", socket.getPort());
        ResultInfo resultInfo = new ResultInfo(this.objectKey);
        Object requestObj = null;
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            requestObj = inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            resultInfo.setException(e);
            resultInfo.setStatus(0);
        }

        if (requestObj instanceof RequestInfo) {
            methodName = ((RequestInfo) requestObj).getMethodName();
            args = ((RequestInfo) requestObj).getArgs();
            argTypes = ((RequestInfo) requestObj).getArgTypes();
            objectKey = ((RequestInfo) requestObj).getObjectKey();
            System.out.printf("[R_S]>> Request from remote port: %d; for method: %s\n", socket.getPort(), methodName);
            try {
                if (objectKey == this.objectKey) {
                    Method m = this.obj.getClass().getMethod(methodName, argTypes);
                    result = m.invoke(obj, args);
                    resultInfo.setResult(result);
                    resultInfo.setStatus((result == null) ? 1 : 2);
                } else {
                    resultInfo.setException(new Exception("Invalid ObjectKey"));
                    resultInfo.setStatus(-1);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                resultInfo.setException(e);
                resultInfo.setStatus(0);
            }
        } else {
            resultInfo.setException(new Exception("Invalid Stub"));
            resultInfo.setStatus(-1);
        }

        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(resultInfo);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.printf("[R_S]>> End of invocation of method: %s\n", methodName);
        }
    }
}
