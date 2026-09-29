package com.warehouse.monitoring.central.service;

import com.warehouse.monitoring.domain.dto.SensorMeasurementDto;

public interface AlarmService {

  void handle(SensorMeasurementDto measurement);
}
