package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  public Map<String, String> getMovieSource(RemoteWebDriver driver, String playUrl) throws IOException {
    Map<String, String> result = new HashMap<>();
    Set<String> urls = new HashSet<>();
    Set<String> srt = new HashSet<>();
    driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
    int tried = 1;
    while (tried < 6 && getSourceFromSet(urls) == null) {
      try {
        if (tried == 1) {
          driver.get(playUrl);
        }
      } catch (Exception ex) {
        LOGGER.info("timeout exceeded. tried = " + tried);
        checkLog(driver, urls, srt);
        if (getSourceFromSet(urls) == null) {
          int retryNavigate = 1;
          while (retryNavigate < 3) {
            try {
              LOGGER.info("trying to refresh browser");
              driver.navigate().refresh();
              LOGGER.info("refresh successfully");
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
    result.put("source", getSourceFromSet(urls));
    result.put("subTitle", srt.isEmpty() ? null: srt.stream().findFirst().orElse(null));
    return result;
  }

  private void checkLog(RemoteWebDriver driver, Set<String> urls, Set<String> srt) throws IOException {
    List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();

    LOGGER.info("{} {} log entries found", entries.size(), LogType.PERFORMANCE);
    for (LogEntry entry : entries) {
      JsonObject messageObject = gson.fromJson(entry.getMessage(), JsonObject.class);
      JsonObject message = messageObject.getAsJsonObject("message");
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
        if (Objects.equals(type, "XHR")) {
          String url = message
              .getAsJsonObject("params")
              .getAsJsonObject("response")
              .get("url").getAsString();
          if (url.startsWith("https://srtsub.vungtv.com")) {
            srt.add(url);
          }
        }
      }
    }
  }

  @Override
  public List<CrawDTO> crawVideoSource(List<CrawDTO> craws) throws MalformedURLException {
    RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteDriverUrl), desiredCapabilities);
    try {
      craws.forEach(craw -> {
        try {
          Map<String, String> crawResult = this.getMovieSource(driver, craw.getInput());
          craw.setResult(crawResult.get("source"));
          craw.setSubTitle(crawResult.get("subTitle"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
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
