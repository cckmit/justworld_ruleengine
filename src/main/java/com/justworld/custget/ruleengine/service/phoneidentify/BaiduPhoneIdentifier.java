package com.justworld.custget.ruleengine.service.phoneidentify;

import com.justworld.custget.ruleengine.dao.PhoneIdentifyDAO;
import com.justworld.custget.ruleengine.service.bo.PhoneIdentify;
import com.justworld.custget.ruleengine.service.bo.PhoneSegment;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Service
public class BaiduPhoneIdentifier implements IPhoneIdentifier {

    @Value("${phone-identify-server.baidu.url}")
    private String serviceUrl;

    @Override
    public void identify(String phone, Consumer<PhoneSegment> consumer) {

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000).compression(true).afterNettyContextInit(ctx -> {
            ctx.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        }));

        WebClient webClient = WebClient.builder()
                .clientConnector(connector)
                .baseUrl(serviceUrl+phone)
                .build();

        Mono<Map> resultMapMono = webClient.get().retrieve().bodyToMono(Map.class);

        resultMapMono.subscribe(map -> {
            log.trace("phone {} identify by baidu aip result=\n{}",phone,map);
            Map<String,?> detailMap = (Map<String, ?>) ((Map)((Map)map.get("response")).get(phone)).get("detail");
            PhoneSegment result = new PhoneSegment();
            result.setSegment(phone.substring(0,7));
            result.setCity(((Map)((List)detailMap.get("area")).get(0)).get("city")+"");
            result.setProvince(String.valueOf(detailMap.get("province")));
            result.setTelOperator(PhoneOperatorIdentify.getPhoneOperator((String) detailMap.get("operator")));
            result.setIdentifyTime(new Date());
            result.setIdentifyType("BAIDU");
            result.setStatus("1");

            consumer.accept(result);

        });

    }
}
