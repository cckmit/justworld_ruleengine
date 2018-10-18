package com.justworld.custget.ruleengine.service.shorturl;

import java.util.List;
import java.util.Map;

/**
 * 短链接生成接口
 */
public interface IShortUrlGenerator {

    public void convertShortUrl(Map<String, String> urlMap);
}
