package com.warehouse.monitoring.central.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

  @Bean
  public StringJsonMessageConverter converter(ObjectMapper objectMapper) {
    return new StringJsonMessageConverter(objectMapper);
  }

  @Bean
  public CommonErrorHandler kafkaErrorHandler() {
    return new DefaultErrorHandler(new FixedBackOff(0L, 0L));
  }

  @Bean
  public NewTopic measurementTopic(@Value("${kafka.topic.measurement}") String topic) {
    return TopicBuilder.name(topic).partitions(1).build();
  }
}
