package com.warehouse.monitoring.warehouse.config;

import com.warehouse.monitoring.enumeration.SensorType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "warehouse")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseProperties {

  @Valid
  @NotEmpty
  List<SensorBinding> sensors = new ArrayList<>();

  @Getter
  @Setter
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class SensorBinding {

    @NotNull
    SensorType type;

    @Min(0)
    @Max(65535)
    int port;
  }
}
