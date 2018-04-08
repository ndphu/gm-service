package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Episode;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Movie;
import vn.kms.phudnguyen.crawlers.vungtv.repository.EpisodeRepository;
import vn.kms.phudnguyen.crawlers.vungtv.repository.MovieRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class CrawlingServiceImpl implements CrawlingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingServiceImpl.class);

  @Autowired
  private MovieRepository movieRepository;

  @Autowired
  private EpisodeRepository episodeRepository;

  @Autowired
  private Gson gson;
  @Autowired
  private DesiredCapabilities desiredCapabilities;

  @Value("${webdriver.remote.url}")
  private String remoteDriverUrl;


  private boolean isCrawling;
  private WebDriver driver;

  @Override
  public void crawMovies() {
    if (isCrawling) {
      LOGGER.info("A crawling process is already running");
      return;
    }
    isCrawling = true;

    try {
      driver = new RemoteWebDriver(new URL(remoteDriverUrl), desiredCapabilities);
      movieRepository.findAll().forEach(m -> {
        LOGGER.info("Processing [{}] {}", m.getId(), m.getTitle());
        crawMovieSource(m);
      });
    } catch (Exception ex) {
      LOGGER.warn("Fail to crawMovies videos", ex);
    } finally {
      closeDriver(this.driver);
      isCrawling = false;
    }
  }

  private void closeDriver(WebDriver driver) {
    try {
      if (driver != null) {
        driver.close();
        driver.quit();
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to close web driver", e);
    }
  }

  @Override
  public void crawEpisodes() {
    if (isCrawling) {
      LOGGER.info("A crawling process is already running");
      return;
    }
    isCrawling = true;
    try {
      driver = new RemoteWebDriver(new URL(remoteDriverUrl), desiredCapabilities);
      episodeRepository.findAll().forEach(ep -> {
        LOGGER.info("Processing [{}] {}", ep.getId(), ep.getTitle());
        crawEpisodeSource(ep);
      });
    } catch (Exception ex) {
      LOGGER.warn("Fail to crawMovies videos", ex);
    } finally {
      closeDriver(this.driver);

      isCrawling = false;
    }
  }

  private void crawEpisodeSource(Episode ep) {
    if (Objects.isNull(ep.getCrawUrl()) || ep.getCrawUrl().isEmpty()) {
      LOGGER.info("Ignoring invalid episode {} without crawUrl", ep);
      return;
    }
    if (Objects.isNull(ep.getVideoSource()) || ep.getVideoSource().isEmpty()) {
      String source = getMovieSource(ep.getCrawUrl());
      if (source != null) {
        ep.setTitle(driver.findElement(By.cssSelector("h1.title-film-film-1")).getText());
        ep.setSubTitle(driver.findElement(By.cssSelector("h2.title-film-film-2")).getText());
        ep.setVideoSource(source);
        LOGGER.info("Updating episode {}", ep.getId());
        episodeRepository.save(ep);
      }
    } else {
      LOGGER.info("Ignoring video which already had videoSource url");
    }
  }

  private void crawMovieSource(Movie m) {
    if (Objects.isNull(m.getPlayUrl()) || m.getPlayUrl().isEmpty()) {
      LOGGER.info("Ignoring invalid movie {} without playUrl url", m.getId());
      return;
    }
    if (Objects.isNull(m.getVideoSource()) || m.getVideoSource().isEmpty()) {
      String source = getMovieSource(m.getPlayUrl());
      if (source != null) {
        m.setVideoSource(source);
        LOGGER.info("Updating video {}", m.getId());
        movieRepository.save(m);
      }
    } else {
      LOGGER.info("Ignoring video which already had videoSource url");
    }
  }

  @Override
  public String getMovieSource(String playUrl) {
    driver.get(playUrl);

    List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
    LOGGER.info(entries.size() + " " + LogType.PERFORMANCE + " log entries found");
    Set<String> urls = new HashSet<>();
    for (LogEntry entry : entries) {
      JsonObject message = gson.fromJson(entry.getMessage(), JsonObject.class).getAsJsonObject("message");
      if (Objects.equals(message.get("method").getAsString(),
          "Network.responseReceived")) {
        String type = message
            .getAsJsonObject("params")
            .get("type").getAsString();
        if (Objects.equals(type, "Media")) {
          urls.add(message
              .getAsJsonObject("params")
              .getAsJsonObject("response")
              .get("url").getAsString());
        }
      }
    }
    LOGGER.info("Found urls: " + urls);

    return getSourceFromSet(urls);
  }

  private String getSourceFromSet(Set<String> urls) {
    Optional<String> source = urls.stream().filter(u -> {
      try {
        return new URL(u).getHost().equals("storage.googleapis.com");
      } catch (MalformedURLException e) {
        LOGGER.warn("Invalid video url {}", u);
        return false;
      }
    }).findFirst();

    if (source.isPresent()) {
      LOGGER.info("Found video videoSource {}", source);
      return source.get();
    } else {
      LOGGER.warn("Cannot find correct video videoSource from videoSource list {}", urls);
      return null;
    }
  }
}
