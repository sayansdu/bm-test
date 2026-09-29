package com.warehouse.monitoring.warehouse.udp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.warehouse.monitoring.enumeration.SensorType;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class UdpSensorEndpointTest {

  @Test
  void start_deliversDatagramPayload() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    List<String> payloads = new CopyOnWriteArrayList<>();
    UdpSensorEndpoint endpoint =
        new UdpSensorEndpoint(
            SensorType.TEMPERATURE,
            0,
            payload -> {
              payloads.add(payload);
              latch.countDown();
            });
    endpoint.start();
    try {
      send(endpoint.port(), "sensor_id=t1; value=30");
      assertTrue(latch.await(5, TimeUnit.SECONDS));
      assertEquals(List.of("sensor_id=t1; value=30"), payloads);
    } finally {
      endpoint.close();
    }
  }

  @Test
  void start_handlerFailure_keepsListening() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    UdpSensorEndpoint endpoint =
        new UdpSensorEndpoint(
            SensorType.HUMIDITY,
            0,
            payload -> {
              latch.countDown();
              if (latch.getCount() == 1) {
                throw new IllegalStateException("boom");
              }
            });
    endpoint.start();
    try {
      send(endpoint.port(), "sensor_id=h1; value=40");
      send(endpoint.port(), "sensor_id=h1; value=41");
      assertTrue(latch.await(5, TimeUnit.SECONDS));
    } finally {
      endpoint.close();
    }
  }

  private void send(int port, String payload) throws Exception {
    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLoopbackAddress(), port));
    }
  }
}
