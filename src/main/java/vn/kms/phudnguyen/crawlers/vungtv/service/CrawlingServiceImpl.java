package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.*;

@Service
public class CrawlingServiceImpl implements CrawlingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingServiceImpl.class);

  @Autowired
  private Gson gson;
  @Autowired
  private DesiredCapabilities desiredCapabilities;

  @Value("${webdriver.remote.url}")
  private String remoteDriverUrl;

  @Override
  public String getMovieSource(RemoteWebDriver driver, String playUrl) {
    Set<String> urls = new HashSet<>();
    driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
    int tried = 1;
    while (tried < 6 && getSourceFromSet(urls) == null) {
      try {
        if (tried == 1) {
          driver.get(playUrl);
        }
      } catch (Exception ex) {
        LOGGER.info("timeout exceeded. tried = " + tried);
        checkLog(driver, urls);
        if (getSourceFromSet(urls) == null) {
          int retryNavigate = 1;
          while (retryNavigate < 3) {
            try {
              driver.navigate().refresh();
              break;
            } catch (Exception ignored) {
              LOGGER.warn("ignore refresh exception");
            } finally {
              retryNavigate++;
              try {
                sleep(5000);
              } catch (Exception ignored) {
                LOGGER.warn("ignore thread interrupt exception");
              }
            }
          }
        }
      } finally {
        tried++;
      }
    }


    LOGGER.info("Found urls: " + urls);
    return getSourceFromSet(urls);
  }

  private void checkLog(RemoteWebDriver driver, Set<String> urls) {
    List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
    LOGGER.info(entries.size() + " " + LogType.PERFORMANCE + " log entries found");
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
  }

  @Override
  public List<CrawDTO> crawVideoSource(List<CrawDTO> craws) throws MalformedURLException {
    RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteDriverUrl), desiredCapabilities);
    try {
      craws.forEach(craw -> craw.setResult(this.getMovieSource(driver, craw.getInput())));
    } catch (Exception ex) {
      LOGGER.error("Fail to craw video", ex);
    } finally {
      driver.close();
      driver.quit();
    }

    return craws;
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
      LOGGER.debug("Found video videoSource {}", source);
      return source.get();
    } else {
      LOGGER.debug("Cannot find correct video videoSource from videoSource list {}", urls);
      return null;
    }
  }
}
