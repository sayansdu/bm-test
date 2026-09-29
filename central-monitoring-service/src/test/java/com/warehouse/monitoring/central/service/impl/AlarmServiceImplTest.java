package com.warehouse.monitoring.central.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehouse.monitoring.central.config.MonitoringProperties;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class AlarmServiceImplTest {

  private AlarmServiceImpl alarmService;

  @BeforeEach
  void setUp() {
    MonitoringProperties properties = new MonitoringProperties();
    properties.setTemperature(35d);
    properties.setHumidity(50d);
    alarmService = new AlarmServiceImpl(properties);
  }

  @Test
  void handle_temperatureAboveThreshold_logsAlarm(CapturedOutput output) {
    alarmService.handle(measurement("t1", SensorType.TEMPERATURE, 36));

    assertTrue(
        output
            .getAll()
            .contains(
                "ALARM: Temperature threshold exceeded | sensorId=t1 | value=36.0°C | threshold=35.0°C"));
  }

  @Test
  void handle_temperatureAtThreshold_doesNotLogAlarm(CapturedOutput output) {
    alarmService.handle(measurement("t1", SensorType.TEMPERATURE, 35));

    assertFalse(output.getAll().contains("ALARM:"));
  }

  @Test
  void handle_humidityAboveThreshold_logsAlarm(CapturedOutput output) {
    alarmService.handle(measurement("h1", SensorType.HUMIDITY, 60));

    assertTrue(
        output
            .getAll()
            .contains(
                "ALARM: Humidity threshold exceeded | sensorId=h1 | value=60.0% | threshold=50.0%"));
  }

  @Test
  void handle_humidityAtThreshold_doesNotLogAlarm(CapturedOutput output) {
    alarmService.handle(measurement("h1", SensorType.HUMIDITY, 50));

    assertFalse(output.getAll().contains("ALARM:"));
  }

  @Test
  void handle_incompleteMeasurement_doesNotThrow(CapturedOutput output) {
    alarmService.handle(null);
    alarmService.handle(measurement(" ", SensorType.TEMPERATURE, 99));

    assertFalse(output.getAll().contains("ALARM:"));
  }

  private SensorMeasurementDto measurement(String sensorId, SensorType type, double value) {
    return new SensorMeasurementDto(sensorId, type, value, Instant.parse("2026-09-29T12:00:00Z"));
  }
}
