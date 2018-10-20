package com.justworld.custget.ruleengine.service.phoneidentify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhoneIdentifierFactory {
    @Autowired
    private BaiduPhoneIdentifier baiduPhoneIdentifier;

    public IPhoneIdentifier getIdentifier(){
        return baiduPhoneIdentifier;

    }
}
