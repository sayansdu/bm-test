package com.warehouse.monitoring.warehouse.udp;

import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;
import com.warehouse.monitoring.enumeration.SensorType;
import com.warehouse.monitoring.warehouse.config.WarehouseProperties;
import com.warehouse.monitoring.warehouse.kafka.producer.MeasurementProducer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UdpIngress implements SmartLifecycle {

  private static final int LOGGED_PAYLOAD_LIMIT = 200;

  WarehouseProperties warehouseProperties;
  MeasurementProducer measurementProducer;

  List<UdpSensorEndpoint> endpoints = new ArrayList<>();
  AtomicBoolean running = new AtomicBoolean(false);

  @Override
  public void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }
    try {
      validateSensors();
      for (WarehouseProperties.SensorBinding sensor : warehouseProperties.getSensors()) {
        UdpSensorEndpoint endpoint =
            new UdpSensorEndpoint(
                sensor.getType(),
                sensor.getPort(),
                payload -> onPayload(sensor.getType(), payload));
        endpoint.start();
        endpoints.add(endpoint);
        log.info(
            "Listening for {} measurements on UDP port {}", sensor.getType(), endpoint.port());
      }
    } catch (RuntimeException ex) {
      stop();
      throw ex;
    }
  }

  @Override
  public void stop() {
    running.set(false);
    for (UdpSensorEndpoint endpoint : endpoints) {
      endpoint.close();
    }
    endpoints.clear();
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  public int port(SensorType type) {
    return endpoints.stream()
        .filter(endpoint -> endpoint.type() == type)
        .map(UdpSensorEndpoint::port)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("UDP listener is not bound for " + type));
  }

  private void validateSensors() {
    List<WarehouseProperties.SensorBinding> sensors = warehouseProperties.getSensors();
    if (sensors == null || sensors.isEmpty()) {
      throw new IllegalStateException("No UDP sensors configured");
    }
    Set<Integer> ports = new HashSet<>();
    for (WarehouseProperties.SensorBinding sensor : sensors) {
      if (sensor.getPort() != 0 && !ports.add(sensor.getPort())) {
        throw new IllegalStateException("Duplicate UDP port " + sensor.getPort());
      }
    }
  }

  private void onPayload(SensorType type, String payload) {
    MeasurementParser.parse(type, payload, Instant.now())
        .ifPresentOrElse(this::publish, () -> logMalformed(type, payload));
  }

  private void publish(SensorMeasurementDto measurement) {
    try {
      measurementProducer.send(measurement);
    } catch (RuntimeException ex) {
      log.error("Failed to publish measurement {}", measurement.getSensorId(), ex);
    }
  }

  private void logMalformed(SensorType type, String payload) {
    String logged = payload;
    if (payload.length() > LOGGED_PAYLOAD_LIMIT) {
      logged = payload.substring(0, LOGGED_PAYLOAD_LIMIT);
    }
    log.warn("Discarding malformed {} datagram: '{}'", type, logged);
  }
}
