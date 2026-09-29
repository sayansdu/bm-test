# Warehouse monitoring

Sensors publish temperature and humidity over UDP. The warehouse service collects those readings and publishes them to Kafka. The central monitoring service consumes the stream and writes an alarm to the log when a reading exceeds its threshold.

```
sensor --UDP--> warehouse-service --Kafka--> central-monitoring-service --log--> ALARM
```

| Sensor | UDP port | Payload | Alarm when |
| --- | --- | --- | --- |
| Temperature | 3344 | `sensor_id=t1; value=30` | value > 35°C |
| Humidity | 3355 | `sensor_id=h1; value=40` | value > 50% |

A reading equal to the threshold is not an alarm.

## Requirements

- Java 21
- Docker, for the local Kafka broker

## Run

Start Kafka:

```bash
docker compose up -d
```

In two terminals:

```bash
./mvnw -pl central-monitoring-service -am spring-boot:run
./mvnw -pl warehouse-service -am spring-boot:run
```

Central monitoring listens on `8080`, the warehouse service on `8081`. Health checks: `/actuator/health`.

Publish a sample that stays under the threshold, then one that crosses it:

```bash
printf 'sensor_id=t1; value=30\n' | nc -u -w1 127.0.0.1 3344
printf 'sensor_id=t1; value=36\n' | nc -u -w1 127.0.0.1 3344
printf 'sensor_id=h1; value=60\n' | nc -u -w1 127.0.0.1 3355
```

The central monitoring console prints:

```text
ALARM: Temperature threshold exceeded | sensorId=t1 | value=36.0°C | threshold=35.0°C
ALARM: Humidity threshold exceeded | sensorId=h1 | value=60.0% | threshold=50.0%
```

Thresholds and ports are in `application.yaml` of each service. Kafka bootstrap servers default to `localhost:9092` (`KAFKA_BOOTSTRAP_SERVERS`).

## Test

```bash
./mvnw test
```

Tests use an embedded broker, so Docker is not required for `./mvnw test`.
