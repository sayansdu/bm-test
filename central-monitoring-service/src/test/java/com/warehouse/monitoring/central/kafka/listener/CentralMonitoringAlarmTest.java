package com.warehouse.monitoring.central.kafka.listener;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = "warehouse.measurement",
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@ExtendWith(OutputCaptureExtension.class)
class CentralMonitoringAlarmTest {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void consume_temperatureAboveThreshold_logsAlarm(CapturedOutput output) throws Exception {
    kafkaTemplate
        .send("warehouse.measurement", "t1", json("t1", SensorType.TEMPERATURE, 36))
        .get(10, TimeUnit.SECONDS);

    await()
        .atMost(Duration.ofSeconds(15))
        .until(() -> output.getAll().contains("ALARM: Temperature threshold exceeded"));
  }

  @Test
  void consume_humidityWithinThreshold_doesNotLogAlarm(CapturedOutput output) throws Exception {
    kafkaTemplate
        .send("warehouse.measurement", "h1", json("h1", SensorType.HUMIDITY, 40))
        .get(10, TimeUnit.SECONDS);

    await()
        .atMost(Duration.ofSeconds(15))
        .until(() -> output.getAll().contains("Received measurement sensorId=h1"));
    assertFalse(output.getAll().contains("ALARM: Humidity threshold exceeded | sensorId=h1"));
  }

  private String json(String sensorId, SensorType type, double value)
      throws JsonProcessingException {
    return objectMapper.writeValueAsString(
        new SensorMeasurementDto(sensorId, type, value, Instant.parse("2026-09-29T12:00:00Z")));
  }
}
