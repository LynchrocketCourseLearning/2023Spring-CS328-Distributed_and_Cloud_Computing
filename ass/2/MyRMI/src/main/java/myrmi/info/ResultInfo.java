package myrmi.info;

import java.io.Serializable;

/**
 * deprecated, using Java API to serialize
 * */
@Deprecated
public class ResultInfo implements Serializable {
    private Object result;
    private Exception exception;
    private int status;
    private int objKey;

    public ResultInfo(int objKey) {
        this.objKey = objKey;
    }

    public ResultInfo(Object result, Exception exception, int objKey) {
        this.result = result;
        this.exception = exception;
        this.objKey = objKey;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getObjKey() {
        return objKey;
    }

    public void setObjKey(int objKey) {
        this.objKey = objKey;
    }
}
