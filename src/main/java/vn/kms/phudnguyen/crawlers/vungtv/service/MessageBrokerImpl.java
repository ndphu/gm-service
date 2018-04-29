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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;
import vn.kms.phudnguyen.crawlers.vungtv.event.CrawEvent;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageBrokerImpl implements MessageBroker, MqttCallback, ApplicationListener<CrawEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageBrokerImpl.class);
  public static final String TOPIC_CRAWLER_REQUEST = "/topic/crawler/request";
  public static final String TOPIC_CRAWLER_RESPONSE_PREFIX = "/topic/crawler/response/";
  public static final String UTF_8 = "UTF-8";

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  CrawlingService crawlingService;

  @Value("${crawler.mqtt.enabled:false}")
  Boolean enableMQTT;

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
        LOGGER.info("Publishing application event");
        applicationEventPublisher.publishEvent(new CrawEvent(this, message));
        LOGGER.info("Published application event");
        break;
      default:
        LOGGER.warn("Unhandled message from topic {}", topic);
    }
    LOGGER.info("Done message arrived");
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {

  }

  @PostConstruct
  public void init() throws MqttException {
    if (enableMQTT) {
      checkConnection();
    }
  }

  public void checkConnection() throws MqttException {
    if (!mqttClient.isConnected()) {
      mqttClient.connect(mqttConnectOptions);
      mqttClient.setCallback(this);
      mqttClient.subscribe(TOPIC_CRAWLER_REQUEST);
      LOGGER.info("Subscribed to {}", TOPIC_CRAWLER_REQUEST);
    }
  }

  @Override
  public void onApplicationEvent(CrawEvent event) {
    LOGGER.info("On application event {}", event);
    MqttMessage message = event.getMessage();
    JsonObject request = null;
    try {
      request = gson.fromJson(new String(message.getPayload(), UTF_8), JsonObject.class);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
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
    String responsePayload = null;
    try {
      responsePayload = gson.toJson(crawlingService.crawVideoSourceWithPool(dtos));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    LOGGER.info("Posting response to {} with payload {}", responseTo, responsePayload);
    try {
      mqttClient.publish(responseTo, new MqttMessage(responsePayload.getBytes(UTF_8)));
    } catch (MqttException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
