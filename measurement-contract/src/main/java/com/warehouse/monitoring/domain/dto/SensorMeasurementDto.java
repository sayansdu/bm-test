package com.warehouse.monitoring.domain.dto;

import com.warehouse.monitoring.enumeration.SensorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorMeasurementDto {

  @NotBlank
  String sensorId;

  @NotNull
  SensorType type;

  double value;

  @NotNull
  Instant observedAt;
}
