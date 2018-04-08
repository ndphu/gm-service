package vn.kms.phudnguyen.crawlers.vungtv.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.kms.phudnguyen.crawlers.vungtv.service.CrawlingService;

import java.net.MalformedURLException;

@Component
public class CrawlerScheduler {
  @Autowired
  CrawlingService crawlingService;

  @Value("${crawler.movie.enabled:false}")
  Boolean enableMovieCrawler;

  @Value("${crawler.episode.enabled:false}")
  Boolean enableEpisodeCrawler;

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
}
