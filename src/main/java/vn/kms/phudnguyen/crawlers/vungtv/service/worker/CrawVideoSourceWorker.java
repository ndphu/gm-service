package vn.kms.phudnguyen.crawlers.vungtv.service.worker;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Episode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Builder
@Data
public class CrawVideoSourceWorker extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawVideoSourceWorker.class);
  private List<Episode> episodes;
  private RemoteWebDriver driver;

  @Override
  public void run() {
    episodes.forEach(this::crawEpisodeSource);
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
      }
    } else {
      LOGGER.info("Ignoring video which already had videoSource url");
    }
  }

  public String getMovieSource(String playUrl) {
    driver.get(playUrl);

    List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
    LOGGER.info(entries.size() + " " + LogType.PERFORMANCE + " log entries found");
    Set<String> urls = new HashSet<>();
    for (LogEntry entry : entries) {
      JsonObject message = new Gson().fromJson(entry.getMessage(), JsonObject.class).getAsJsonObject("message");
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
