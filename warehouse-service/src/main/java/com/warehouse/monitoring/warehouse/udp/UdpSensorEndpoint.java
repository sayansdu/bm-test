package com.warehouse.monitoring.warehouse.udp;

import com.warehouse.monitoring.enumeration.SensorType;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class UdpSensorEndpoint implements AutoCloseable {

  private static final int BUFFER_SIZE = 2048;
  private static final int SOCKET_TIMEOUT_MILLIS = 500;

  private final SensorType type;
  private final int requestedPort;
  private final Consumer<String> handler;
  private final AtomicBoolean running = new AtomicBoolean(false);

  private DatagramSocket socket;
  private Thread worker;
  private int boundPort;

  UdpSensorEndpoint(SensorType type, int requestedPort, Consumer<String> handler) {
    this.type = type;
    this.requestedPort = requestedPort;
    this.handler = handler;
  }

  void start() {
    if (!running.compareAndSet(false, true)) {
      return;
    }
    try {
      socket = new DatagramSocket(null);
      socket.setReuseAddress(true);
      socket.bind(new InetSocketAddress(requestedPort));
      socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
      boundPort = socket.getLocalPort();
    } catch (IOException ex) {
      running.set(false);
      closeSocket();
      throw new IllegalStateException("Unable to bind UDP port " + requestedPort, ex);
    }
    worker =
        Thread.ofVirtual()
            .name("udp-" + type.name().toLowerCase(Locale.ROOT) + "-" + boundPort)
            .start(this::receiveLoop);
  }

  SensorType type() {
    return type;
  }

  int port() {
    return boundPort;
  }

  private void receiveLoop() {
    byte[] buffer = new byte[BUFFER_SIZE];
    while (running.get()) {
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(packet);
      } catch (SocketTimeoutException ignored) {
        continue;
      } catch (IOException ex) {
        if (running.get()) {
          log.error("UDP receive failed for {} on port {}", type, boundPort, ex);
        }
        break;
      }
      String payload =
          new String(
                  packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8)
              .trim();
      if (payload.isEmpty()) {
        continue;
      }
      try {
        handler.accept(payload);
      } catch (RuntimeException ex) {
        log.error("UDP handler failed for {}", type, ex);
      }
    }
  }

  @Override
  public void close() {
    running.set(false);
    closeSocket();
    if (worker != null) {
      try {
        worker.join(Duration.ofSeconds(2));
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private void closeSocket() {
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
  }
}
