package com.warehouse.monitoring.warehouse.udp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MeasurementParserTest {

  private static final Instant OBSERVED_AT = Instant.parse("2026-09-29T12:00:00Z");

  @Test
  void parse_exactTemperatureSample() {
    Optional<SensorMeasurementDto> parsed =
        MeasurementParser.parse(SensorType.TEMPERATURE, "sensor_id=t1; value=30", OBSERVED_AT);

    assertTrue(parsed.isPresent());
    assertEquals("t1", parsed.get().getSensorId());
    assertEquals(SensorType.TEMPERATURE, parsed.get().getType());
    assertEquals(30d, parsed.get().getValue());
    assertEquals(OBSERVED_AT, parsed.get().getObservedAt());
  }

  @Test
  void parse_exactHumiditySample() {
    Optional<SensorMeasurementDto> parsed =
        MeasurementParser.parse(SensorType.HUMIDITY, "sensor_id=h1; value=40", OBSERVED_AT);

    assertTrue(parsed.isPresent());
    assertEquals("h1", parsed.get().getSensorId());
    assertEquals(SensorType.HUMIDITY, parsed.get().getType());
    assertEquals(40d, parsed.get().getValue());
  }

  @Test
  void parse_toleratesWhitespaceAndCase() {
    Optional<SensorMeasurementDto> parsed =
        MeasurementParser.parse(
            SensorType.TEMPERATURE, "  SENSOR_ID = t1 ; VALUE = -3.5  ", OBSERVED_AT);

    assertTrue(parsed.isPresent());
    assertEquals("t1", parsed.get().getSensorId());
    assertEquals(-3.5d, parsed.get().getValue());
  }

  @Test
  void parse_malformedPayload_returnsEmpty() {
    assertTrue(MeasurementParser.parse(SensorType.TEMPERATURE, "value=30", OBSERVED_AT).isEmpty());
    assertTrue(
        MeasurementParser.parse(SensorType.TEMPERATURE, "sensor_id=t1; value=abc", OBSERVED_AT)
            .isEmpty());
    assertTrue(MeasurementParser.parse(SensorType.TEMPERATURE, "", OBSERVED_AT).isEmpty());
    assertTrue(MeasurementParser.parse(SensorType.TEMPERATURE, null, OBSERVED_AT).isEmpty());
  }
}
