package com.warehouse.monitoring.warehouse.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic measurementTopic(@Value("${kafka.topic.measurement}") String topic) {
    return TopicBuilder.name(topic).partitions(1).build();
  }
}
