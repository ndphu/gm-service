package vn.kms.phudnguyen.crawlers.vungtv.service;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface MessageBroker {
  void checkConnection() throws MqttException;
}
