package vn.kms.phudnguyen.crawlers.vungtv.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kms.phudnguyen.crawlers.vungtv.service.CrawlingService;

import java.net.MalformedURLException;

@Component
public class CrawlerScheduler {
  @Autowired
  CrawlingService crawlingService;

  @Scheduled(fixedDelay = 3600000)
  public void craw() throws MalformedURLException {
    crawlingService.craw();
  }
}
