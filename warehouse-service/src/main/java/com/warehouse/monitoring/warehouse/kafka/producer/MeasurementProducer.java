package com.warehouse.monitoring.warehouse.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeasurementProducer {

  @Value("${kafka.topic.measurement}")
  private String measurementTopic;

  private final ObjectMapper objectMapper;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public void send(SensorMeasurementDto measurement) {
    try {
      String message = objectMapper.writeValueAsString(measurement);
      log.info("Sending measurement to kafka {}, sensorId={}", message, measurement.getSensorId());
      kafkaTemplate
          .send(measurementTopic, measurement.getSensorId(), message)
          .whenComplete(
              (result, ex) -> {
                if (ex != null) {
                  log.error("Error while sending measurement {}", measurement.getSensorId(), ex);
                }
              });
    } catch (JsonProcessingException e) {
      log.error("Error while serializing measurement {}", measurement.getSensorId(), e);
    }
  }
}
