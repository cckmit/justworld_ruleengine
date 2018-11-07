package com.justworld.custget.intf.common;

import lombok.Data;

@Data
public class RequestHead {
    private String username;
    private String password;
    private String seq;
}
