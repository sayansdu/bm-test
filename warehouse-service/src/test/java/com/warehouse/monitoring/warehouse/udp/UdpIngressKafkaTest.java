package com.warehouse.monitoring.warehouse.udp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehouse.monitoring.enumeration.SensorType;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@SpringBootTest(
    properties = {
      "warehouse.sensors[0].type=TEMPERATURE",
      "warehouse.sensors[0].port=0",
      "warehouse.sensors[1].type=HUMIDITY",
      "warehouse.sensors[1].port=0"
    })
@EmbeddedKafka(
    partitions = 1,
    topics = "warehouse.measurement",
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class UdpIngressKafkaTest {

  @Autowired
  private UdpIngress udpIngress;

  @Autowired
  private EmbeddedKafkaBroker broker;

  @Test
  void datagram_isPublishedToKafka() throws Exception {
    assertTrue(udpIngress.port(SensorType.TEMPERATURE) > 0);
    assertTrue(udpIngress.port(SensorType.HUMIDITY) > 0);

    Map<String, Object> consumerProps =
        KafkaTestUtils.consumerProps("warehouse-udp-test", "true", broker);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    try (Consumer<String, String> consumer =
        new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()) {
      broker.consumeFromAnEmbeddedTopic(consumer, "warehouse.measurement");
      send(udpIngress.port(SensorType.TEMPERATURE), "sensor_id=t1; value=36");

      ConsumerRecord<String, String> record =
          KafkaTestUtils.getSingleRecord(
              consumer, "warehouse.measurement", Duration.ofSeconds(15));
      assertTrue(record.value().contains("\"sensorId\":\"t1\""));
      assertTrue(record.value().contains("\"type\":\"TEMPERATURE\""));
      assertTrue(record.value().contains("36"));
    }
  }

  private void send(int port, String payload) throws Exception {
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLoopbackAddress(), port));
    }
  }
}
