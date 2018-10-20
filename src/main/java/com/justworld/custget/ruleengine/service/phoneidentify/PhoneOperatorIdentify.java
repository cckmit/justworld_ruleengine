package com.justworld.custget.ruleengine.service.phoneidentify;

public class PhoneOperatorIdentify {

    /**
     * 根据运营商描述进行运营商分类，如无法识别则原样返回
     * @param desc
     * @return
     */
    public static String getPhoneOperator(String desc){
        if(desc.contains("移动")){
            return "1";
        } else if(desc.contains("联通")){
            return "2";
        } else if(desc.contains("电信")) {
            return "3";
        } else {
            return desc;
        }
    }
}
