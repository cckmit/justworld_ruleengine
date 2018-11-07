package com.justworld.custget.intf.common;

import lombok.Data;

@Data
public class BaseRequest<Body> {
    private RequestHead head;
    private Body body;
}
