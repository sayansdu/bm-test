package com.warehouse.monitoring.central.config;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "monitoring.threshold")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonitoringProperties {

  @NotNull
  Double temperature;

  @NotNull
  Double humidity;
}
