package com.warehouse.monitoring.central.kafka.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.warehouse.monitoring.central.service.AlarmService;
import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MeasurementListenerTest {

  @Test
  void consume_delegatesToAlarmService() {
    AlarmService alarmService = mock(AlarmService.class);
    MeasurementListener listener = new MeasurementListener(alarmService);
    SensorMeasurementDto measurement =
        new SensorMeasurementDto("t1", SensorType.TEMPERATURE, 36, Instant.EPOCH);

    listener.consume(measurement);

    verify(alarmService).handle(measurement);
  }

  @Test
  void consume_whenHandlingFails_doesNotThrow() {
    AlarmService alarmService = mock(AlarmService.class);
    SensorMeasurementDto measurement =
        new SensorMeasurementDto("t1", SensorType.TEMPERATURE, 36, Instant.EPOCH);
    doThrow(new IllegalStateException("boom")).when(alarmService).handle(measurement);
    MeasurementListener listener = new MeasurementListener(alarmService);

    assertDoesNotThrow(() -> listener.consume(measurement));
  }
}
