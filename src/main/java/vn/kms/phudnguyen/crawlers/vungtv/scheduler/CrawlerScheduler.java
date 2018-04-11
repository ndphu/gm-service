package vn.kms.phudnguyen.crawlers.vungtv.scheduler;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kms.phudnguyen.crawlers.vungtv.service.CrawlingService;
import vn.kms.phudnguyen.crawlers.vungtv.service.MessageBroker;
import vn.kms.phudnguyen.crawlers.vungtv.service.MessageBrokerImpl;

import java.net.MalformedURLException;

@Component
public class CrawlerScheduler {
  @Autowired
  CrawlingService crawlingService;

  @Value("${crawler.movie.enabled:false}")
  Boolean enableMovieCrawler;

  @Value("${crawler.episode.enabled:false}")
  Boolean enableEpisodeCrawler;

  @Autowired
  MessageBroker messageBroker;

  @Scheduled(fixedDelay = 3600000)
  public void crawMovies(){
    if (enableMovieCrawler) {
      crawlingService.crawMovies();
    }
  }

  @Scheduled(fixedDelay = 15000)
  public void crawEpisodes() throws MalformedURLException {
    if (enableEpisodeCrawler) {
      crawlingService.crawEpisodes();
    }
  }

  @Scheduled(fixedDelay = 15000)
  public void checkMqttConnection() throws MqttException {
    messageBroker.checkConnection();
  }
}
