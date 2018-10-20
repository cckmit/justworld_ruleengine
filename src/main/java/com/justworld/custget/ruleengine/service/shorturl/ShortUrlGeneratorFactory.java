package com.justworld.custget.ruleengine.service.shorturl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShortUrlGeneratorFactory {
    @Autowired
    private SinaShortUrlGenerator sinaShortUrlGenerator;

    public IShortUrlGenerator getGenerator(){
        return sinaShortUrlGenerator;
    }
}
