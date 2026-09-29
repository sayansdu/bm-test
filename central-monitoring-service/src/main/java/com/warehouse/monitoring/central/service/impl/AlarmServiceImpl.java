package com.warehouse.monitoring.central.service.impl;

import com.warehouse.monitoring.central.config.MonitoringProperties;
import com.warehouse.monitoring.central.service.AlarmService;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlarmServiceImpl implements AlarmService {

  MonitoringProperties monitoringProperties;

  @PostConstruct
  void logThresholds() {
    log.info(
        "Thresholds configured: temperature={}°C, humidity={}%",
        monitoringProperties.getTemperature(),
        monitoringProperties.getHumidity());
  }

  @Override
  public void handle(SensorMeasurementDto measurement) {
    if (measurement == null
        || measurement.getSensorId() == null
        || measurement.getSensorId().isBlank()
        || measurement.getType() == null) {
      log.warn("Discarding incomplete measurement: {}", measurement);
      return;
    }
    double threshold = thresholdFor(measurement.getType());
    log.info(
        "Received measurement sensorId={} type={} value={}",
        measurement.getSensorId(),
        measurement.getType(),
        measurement.getValue());
    if (Double.compare(measurement.getValue(), threshold) > 0) {
      log.error(alarmMessage(measurement, threshold));
    }
  }

  private double thresholdFor(SensorType type) {
    return switch (type) {
      case TEMPERATURE -> monitoringProperties.getTemperature();
      case HUMIDITY -> monitoringProperties.getHumidity();
    };
  }

  private String alarmMessage(SensorMeasurementDto measurement, double threshold) {
    String label = measurement.getType() == SensorType.TEMPERATURE ? "Temperature" : "Humidity";
    String unit = measurement.getType() == SensorType.TEMPERATURE ? "°C" : "%";
    return "ALARM: %s threshold exceeded | sensorId=%s | value=%s%s | threshold=%s%s"
        .formatted(
            label,
            measurement.getSensorId(),
            Double.toString(measurement.getValue()),
            unit,
            Double.toString(threshold),
            unit);
  }
}
