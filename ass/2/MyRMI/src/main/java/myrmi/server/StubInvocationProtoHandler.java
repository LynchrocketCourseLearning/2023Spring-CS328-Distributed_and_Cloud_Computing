package myrmi.server;

import com.google.protobuf.ByteString;
import myrmi.info.RequestInfoProto;
import myrmi.info.ResultInfoProto;
import myrmi.exception.RemoteException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * using Protobuf to serialize & deserialize
 */
public class StubInvocationProtoHandler implements InvocationHandler, Serializable {
    private String host;
    private int port;
    private int objectKey;

    public StubInvocationProtoHandler(String host, int port, int objectKey) {
        this.host = host;
        this.port = port;
        this.objectKey = objectKey;
        System.out.printf("[C]>> Stub created to %s:%d, object key = %d\n", host, port, objectKey);
    }

    public StubInvocationProtoHandler(RemoteObjectRef ref) {
        this(ref.getHost(), ref.getPort(), ref.getObjectKey());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException, IOException, ClassNotFoundException, Throwable {
        Object result = null;
        Exception exception = null;
        int status;
        int objectKey;
        /*TODO: implement stub proxy invocation handler here
         *  You need to do:
         * 1. connect to remote skeleton, send method and arguments
         * 2. get result back and return to caller transparently
         * */
        System.out.printf("[I_C]>> StubInvocationHandler invokes method: %s; on the port: %d\n", method.getName(), port);
        try (Socket client = new Socket(host, port)) {
            // arguments are originally as Object, here we need to turn it to com.google.protobuf.ByteString
            // be careful with void parameter
            List<ByteString> argsList =
                    (args == null || args.length == 0) ? new ArrayList<>()
                            : Arrays.stream(args)
                            .map(x -> ByteString.copyFrom(SerializationUtils.serialize((Serializable) x)))
                            .collect(Collectors.toList());
            // types of arguments are originally as Class<?>, here we need to turn it to the String name of the Class<?>
            List<String> argTypesList =
                    (args == null || args.length == 0) ? new ArrayList<>()
                            : Arrays.stream(args)
                            .map(Object::getClass)
                            .map(Class::getName)
                            .collect(Collectors.toList());
            RequestInfoProto.RequestInfo requestInfo = RequestInfoProto.RequestInfo.newBuilder()
                    .setMethodName(method.getName())
                    .setObjectKey(this.objectKey)
                    .addAllArgs(argsList)
                    .addAllArgTypes(argTypesList)
                    .build();

            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.writeInt(requestInfo.getSerializedSize());
            outputStream.write(requestInfo.toByteArray());
            outputStream.flush();

            DataInputStream inputStream = new DataInputStream(client.getInputStream());
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
            ResultInfoProto.ResultInfo resultInfo = ResultInfoProto.ResultInfo.parseFrom(buffer);
            if (resultInfo != null) {
                result = (resultInfo.getResult().isEmpty()) ? null
                        : SerializationUtils.deserialize(resultInfo.getResult().toByteArray());
                exception = (resultInfo.getException().isEmpty()) ? null
                        : SerializationUtils.deserialize(resultInfo.getException().toByteArray());
                status = resultInfo.getStatus();
                objectKey = resultInfo.getObjectKey();
                if (objectKey != this.objectKey) {
                    throw new Exception("Invalid ObjectKey");
                }
                if (status == -1) {
                    System.out.println("[R_C]>> Invocation Error");
                } else if (status == 0) {
                    assert exception != null;
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
