package com.justworld.custget.ruleengine.common;

import java.io.Serializable;

public class BaseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String rtcd;    //返回码 0=成功
    private String msg;     //返回消息
    private T data;

    public BaseResult() {
    }

    public BaseResult(String rtcd, String msg) {
        this.rtcd = rtcd;
        this.msg = msg;
    }

    public BaseResult(String rtcd, String msg, T t) {
        this.rtcd = rtcd;
        this.msg = msg;
        this.data = t;
    }

    public static BaseResult buildSuccess(){
        return new BaseResult("0",null);
    }


    public static BaseResult buildFail(String rtcd, String msg){
        return new BaseResult(rtcd,msg);
    }

    public static <T> BaseResult<T> buildSuccess(T data){
        return new BaseResult<>("0",null,data);
    }

    public String getRtcd() {
        return rtcd;
    }

    public void setRtcd(String rtcd) {
        this.rtcd = rtcd;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
