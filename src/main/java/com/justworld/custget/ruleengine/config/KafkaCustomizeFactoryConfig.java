package com.justworld.custget.ruleengine.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaCustomizeFactoryConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokers;

    private String group1 = "BatchConsumerGroup";

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerBatchConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = getDefaultFactory();
        Map<String, Object> properties = getCommonPropertis();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "BatchConsumerGroup");
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
        return factory;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerSinaShortUrlFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = getDefaultFactory();
        Map<String, Object> properties = getCommonPropertis();
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "SinaShortUrlGroup");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
        return factory;
    }

    private ConcurrentKafkaListenerContainerFactory<String, String> getDefaultFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        factory.getContainerProperties().setPollTimeout(2000);
        return factory;
    }

    public Map<String, Object> getCommonPropertis() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return properties;
    }
}
