package vn.kms.phudnguyen.crawlers.vungtv.event;

import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.context.ApplicationEvent;

@Data
public class CrawEvent extends ApplicationEvent {
  private final MqttMessage message;

  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public CrawEvent(Object source, MqttMessage message) {
    super(source);
    this.message = message;
  }
}
