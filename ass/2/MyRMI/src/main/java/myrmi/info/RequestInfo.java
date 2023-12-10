package myrmi.info;

import java.io.Serializable;

/**
 * deprecated, using Java API to serialize
 * */
@Deprecated
public class RequestInfo implements Serializable {
    private String methodName;
    private Object[] args;
    private Class<?>[] argTypes;
    private int objectKey;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(Class<?>[] argTypes) {
        this.argTypes = argTypes;
    }

    public int getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(int objectKey) {
        this.objectKey = objectKey;
    }

    public RequestInfo(String methodName, Object[] args, Class<?>[] argTypes, int objectKey) {
        this.methodName = methodName;
        this.args = args;
        this.argTypes = argTypes;
        this.objectKey = objectKey;
    }
}
