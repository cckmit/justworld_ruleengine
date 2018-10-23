package com.justworld.custget.ruleengine.service.phoneidentify;

import com.justworld.custget.ruleengine.dao.PhoneIdentifyDAO;
import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BaiduPhoneIdentifier implements IPhoneIdentifier {

    @Value("${phone-identify-server.baidu.url}")
    private String serviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public PhoneIdentify identify(String phone) {

        Map<String,?> resultMap = restTemplate.getForObject(serviceUrl,Map.class,phone);
        log.trace("phone {} identify by baidu aip result=\n{}",phone,resultMap);

        //封装结果
        Map<String,?> detailMap = (Map<String, ?>) ((Map)((Map)resultMap.get("response")).get(phone)).get("detail");
        PhoneIdentify result = new PhoneIdentify();
        result.setPhone(phone);
        result.setCity(((Map)((List)detailMap.get("area")).get(0)).get("city")+"");
        result.setProvince(String.valueOf(detailMap.get("province")));
        result.setTelOperator(PhoneOperatorIdentify.getPhoneOperator((String) detailMap.get("operator")));
        result.setIdentifyTime(new Date());
        result.setIdentifyType("BAIDU");

        return result;
    }
}
