package com.justworld.custget.ruleengine.service.phoneidentify;

import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;

/**
 * 手机号码识别器
 */
public interface IPhoneIdentifier {

    public PhoneIdentify identify(String phone);
}
