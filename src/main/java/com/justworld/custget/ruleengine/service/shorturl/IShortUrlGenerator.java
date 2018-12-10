package com.justworld.custget.ruleengine.service.shorturl;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 短链接生成接口
 */
public interface IShortUrlGenerator {

    public void convertShortUrl(Map<String, String> urlMap, Consumer<Map<String,String>> consumer);
}
