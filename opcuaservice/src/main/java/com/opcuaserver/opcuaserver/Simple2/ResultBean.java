package com.opcuaserver.opcuaserver.Simple2;

public class ResultBean {
    private String msg;
    private boolean success;

    private Object result;

    public ResultBean() {
    }


    public ResultBean(boolean success, Object result) {
        this.success = success;
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
