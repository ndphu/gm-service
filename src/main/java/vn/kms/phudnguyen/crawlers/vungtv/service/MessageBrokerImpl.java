package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.plugin2.message.Message;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MessageBrokerImpl implements MessageBroker, MqttCallback {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageBrokerImpl.class);
  public static final String TOPIC_CRAWLER_REQUEST = "/topic/crawler/request";
  public static final String TOPIC_CRAWLER_RESPONSE_PREFIX = "/topic/crawler/response/";
  public static final String UTF_8 = "UTF-8";

  @Autowired
  CrawlingService crawlingService;

  @Autowired
  MqttClient mqttClient;

  @Autowired
  MqttConnectOptions mqttConnectOptions;

  @Autowired
  Gson gson;

  @Override
  public void connectionLost(Throwable cause) {

  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    LOGGER.info("Got message from {}", topic);
    switch (topic) {
      case TOPIC_CRAWLER_REQUEST:
        JsonObject request = gson.fromJson(new String(message.getPayload(), UTF_8), JsonObject.class);
        List<CrawDTO> dtos = new ArrayList<>();
        JsonArray items = request.get("items").getAsJsonArray();
        for (int i = 0; i < items.size(); ++i) {
          JsonObject crawRequest = items.get(i).getAsJsonObject();
          dtos.add(CrawDTO.builder()
              .id(crawRequest.get("id").getAsString())
              .input(crawRequest.get("input").getAsString())
              .build());
        }
        String responseTo = TOPIC_CRAWLER_RESPONSE_PREFIX + request.get("responseTo").getAsString();
        String responsePayload = gson.toJson(crawlingService.crawVideoSource(dtos));
        LOGGER.info("Posting response to {} with payload {}", responseTo, responsePayload);
        mqttClient.publish(responseTo, new MqttMessage(responsePayload.getBytes(UTF_8)));
        break;
      default:
        LOGGER.warn("Unhandled message from topic {}", topic);
    }
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

  public void checkConnection() throws MqttException {
    if (!mqttClient.isConnected()) {
      mqttClient.connect(mqttConnectOptions);
      mqttClient.setCallback(this);
      mqttClient.subscribe(TOPIC_CRAWLER_REQUEST);
    }
  }
}
