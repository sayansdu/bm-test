package com.warehouse.monitoring.warehouse.udp;

import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MeasurementParser {

  private static final Pattern MEASUREMENT =
      Pattern.compile(
          "^sensor_id\\s*=\\s*([^;]+?)\\s*;\\s*value\\s*=\\s*(-?\\d+(?:\\.\\d+)?)\\s*$",
          Pattern.CASE_INSENSITIVE);

  private MeasurementParser() {}

  public static Optional<SensorMeasurementDto> parse(
      SensorType type, String payload, Instant observedAt) {
    if (type == null || payload == null || observedAt == null) {
      return Optional.empty();
    }
    Matcher matcher = MEASUREMENT.matcher(payload.trim());
    if (!matcher.matches()) {
      return Optional.empty();
    }
    String sensorId = matcher.group(1).trim();
    if (sensorId.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new SensorMeasurementDto(sensorId, type, Double.parseDouble(matcher.group(2)), observedAt));
  }
}
