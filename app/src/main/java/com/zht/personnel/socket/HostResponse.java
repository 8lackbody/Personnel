package com.zht.personnel.socket;

public class HostResponse {

    /**
     * 结果码
     */
    private Integer code;

    /**
     * 结果消息
     */
    private String msg;

    /**
     * 结果数据载体
     */
    private Object data;

    public HostResponse() {
    }

    public HostResponse(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
