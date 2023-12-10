package myrmi.server;

import com.google.protobuf.ByteString;
import myrmi.info.RequestInfoProto;
import myrmi.info.ResultInfoProto;
import myrmi.Remote;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;

/**
 * using Protobuf to serialize & deserialize
 */
public class RequestProtoHandler implements Runnable {
    private final Socket socket;
    private final Remote obj;
    private final int objectKey;

    public RequestProtoHandler(Socket socket, Remote remoteObj, int objectKey) {
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
        Object result = null;

        /*TODO: implement method here
         * You need to:
         * 1. handle requests from stub, receive invocation arguments, deserialization
         * 2. get result by calling the real object, and handle different cases (non-void method, void method, method throws exception, exception in invocation process)
         * Hint: you can use an int to represent the cases: -1 invocation error, 0 exception thrown, 1 void method, 2 non-void method
         *  */
        System.out.printf("[R_S]>> RequestProtoHandler get request from socket port: %d\n", socket.getPort());
        ResultInfoProto.ResultInfo.Builder resultInfoBuilder = ResultInfoProto.ResultInfo.newBuilder();
        resultInfoBuilder.setObjectKey(this.objectKey);
        RequestInfoProto.RequestInfo requestInfo = null;
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            int length = inputStream.readInt();
            byte[] buffer = new byte[length];
            int read = 0;
            while (read < length) {
                int n = inputStream.read(buffer, read, length - read);
                if (n < 0) {
                    throw new EOFException("Unexpected end of input stream");
                }
                read += n;
            }
            requestInfo = RequestInfoProto.RequestInfo.parseFrom(buffer);
        } catch (IOException e) {
            resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(e)));
            resultInfoBuilder.setStatus(0);
        }

        if (requestInfo != null) {
            try {
                methodName = requestInfo.getMethodName();
                // from com.google.protobuf.ByteString to Object
                List<ByteString> argsList = requestInfo.getArgsList();
                args = (argsList.size() == 0) ? new Object[0]
                        : argsList.stream().map(x -> SerializationUtils.deserialize(x.toByteArray())).toArray();
                // find Class<?> from class name using reflection
                List<String> argTypesList = requestInfo.getArgTypesList();
                argTypes = (argTypesList.size() == 0) ? new Class[0]
                        : argTypesList.stream().map(x -> {
                    try {
                        return Class.forName(x);
                    } catch (ClassNotFoundException e) {
                        resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(e)));
                        resultInfoBuilder.setStatus(0);
                        throw new RuntimeException(e);
                    }
                }).toArray(Class[]::new);
                objectKey = requestInfo.getObjectKey();
                System.out.printf("[R_S]>> Request from remote port: %d; for method: %s\n", socket.getPort(), methodName);

                if (objectKey == this.objectKey) {
                    Method[] methods = obj.getClass().getMethods();
                    boolean foundMethod = false;
                    for (Method m : methods) {
                        if (m.getName().equals(methodName)) {
                            Class<?>[] parameterTypes = m.getParameterTypes();
                            int n = parameterTypes.length;
                            if (n != argTypes.length) continue;
                            boolean flag = false;
                            for (int i = 0; i < n; i++) {
                                if (!parameterTypes[i].isAssignableFrom(argTypes[i])) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag) continue;
                            result = m.invoke(obj, args);
                            foundMethod = true;
                            break;
                        }
                    }
                    if (foundMethod) {
                        resultInfoBuilder.setResult(ByteString.copyFrom(SerializationUtils.serialize((Serializable) result)));
                        resultInfoBuilder.setStatus((result == null) ? 1 : 2);
                    } else {
                        resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(new NoSuchMethodException())));
                        resultInfoBuilder.setStatus(-1);
                    }
                } else {
                    resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(new Exception("Invalid ObjectKey"))));
                    resultInfoBuilder.setStatus(-1);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(e)));
                resultInfoBuilder.setStatus(0);
            }
        } else {
            resultInfoBuilder.setException(ByteString.copyFrom(SerializationUtils.serialize(new Exception("Invalid Stub"))));
            resultInfoBuilder.setStatus(-1);
        }

        try {
            ResultInfoProto.ResultInfo resultInfo = resultInfoBuilder.build();
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(resultInfo.getSerializedSize());
            outputStream.write(resultInfo.toByteArray());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.printf("[R_S]>> End of invocation of method: %s\n", methodName);
        }
    }
}
