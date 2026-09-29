package com.warehouse.monitoring.central.kafka.listener;

import com.warehouse.monitoring.central.service.AlarmService;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeasurementListener {

  AlarmService alarmService;

  @KafkaListener(
      topics = {"${kafka.topic.measurement}"},
      groupId = "${kafka.group.monitoring}")
  public void consume(@Payload SensorMeasurementDto measurement) {
    log.info("Consume measurement: {}", measurement);
    try {
      alarmService.handle(measurement);
    } catch (Exception e) {
      log.atError()
          .setMessage("Got error during consuming measurement {}")
          .addArgument(measurement)
          .setCause(e)
          .log();
    }
  }
}
