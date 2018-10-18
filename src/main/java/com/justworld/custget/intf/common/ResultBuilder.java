package com.justworld.custget.intf.common;

import java.util.HashMap;
import java.util.Map;

public class ResultBuilder {
    public static Map<String,String> buildResult(boolean isSuccess, String errorCode, String errorMsg){
        Map<String,String> resultMap = new HashMap<>();
        if(isSuccess){
            resultMap.put("rtcd","0");
        }else{
            resultMap.put("rtcd",errorCode);
            resultMap.put("msg",errorMsg);
        }
        return resultMap;
    }
}
