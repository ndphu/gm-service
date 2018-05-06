package vn.kms.phudnguyen.crawlers.vungtv.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQTTConfig {
  @Value("${mqtt.broker.protocol}")
  String protocol;

  @Value("${mqtt.broker.host:localhost}")
  String brokerHost;

  @Value("${mqtt.broker.port:1883}")
  int port;

  @Bean
  public MqttConnectOptions mqttConnectOptions() {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);
    connOpts.setKeepAliveInterval(30);
    return connOpts;
  }

  @Bean
  public MqttClient mqttClient() throws MqttException {
    MqttClient client = new MqttClient(String.format("%s://%s:%d", protocol, brokerHost, port),
        "crawler-" + System.currentTimeMillis());
    return client;
  }
}
