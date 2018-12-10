package com.justworld.custget.ruleengine.service.phoneidentify;

import com.justworld.custget.ruleengine.service.bo.PhoneSegment;

import java.util.function.Consumer;

/**
 * 手机号码识别器
 */
public interface IPhoneIdentifier {

    public void identify(String phone, Consumer<PhoneSegment> consumer);
}
