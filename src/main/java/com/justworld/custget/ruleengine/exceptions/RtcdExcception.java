package com.justworld.custget.ruleengine.exceptions;

import lombok.Getter;

@Getter
public class RtcdExcception extends RuntimeException {
    private String rtcd;
    private String msg;

    public RtcdExcception(String rtcd, String msg) {
        this.rtcd = rtcd;
        this.msg = msg;
    }
}
