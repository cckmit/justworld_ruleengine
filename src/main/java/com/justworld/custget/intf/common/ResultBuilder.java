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

    public static Map<String,String> buildSuccess(){
        Map<String,String> resultMap = new HashMap<>();
            resultMap.put("rtcd","0");
        return resultMap;
    }

    public static Map<String,String> buildFail(String errorCode, String errorMsg){
        Map<String,String> resultMap = new HashMap<>();
        resultMap.put("rtcd",errorCode);
        resultMap.put("msg",errorMsg);
        return resultMap;
    }


}
