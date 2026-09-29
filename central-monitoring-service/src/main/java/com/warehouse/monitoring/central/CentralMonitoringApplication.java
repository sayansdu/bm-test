package com.warehouse.monitoring.central;

import com.warehouse.monitoring.central.config.MonitoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MonitoringProperties.class)
public class CentralMonitoringApplication {

  public static void main(String[] args) {
    SpringApplication.run(CentralMonitoringApplication.class, args);
  }
}
