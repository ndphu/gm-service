package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

import javax.annotation.PostConstruct;
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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@Service
public class CrawlingServiceImpl implements CrawlingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingServiceImpl.class);

  @Autowired
  private Gson gson;

  @Autowired
  private DesiredCapabilities desiredCapabilities;

  @Value("${webdriver.remote.url}")
  private String remoteDriverUrl;

  @Value("${webdriver.thread.count:4}")
  private int threadCount;

  private BlockingQueue<Integer> driverQueue = new LinkedBlockingQueue<>();

  @PostConstruct
  public void postConstruct() {
    LOGGER.info("initializing remote drivers...");

    for (int i = 0; i < threadCount; ++i) {
      LOGGER.info("initializing driver {}", i);
      driverQueue.add(1);
      LOGGER.info("initialized driver {}", i);
    }

    LOGGER.info("finished");
  }

  @Override
  public Map<String, String> getMovieSource(RemoteWebDriver driver, String playUrl) throws IOException {
    Map<String, String> result = new HashMap<>();
    Set<String> urls = new HashSet<>();
    Set<String> srt = new HashSet<>();
    driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
    int tried = 1;
    while (tried < 6 && getSourceFromSet(urls) == null) {
      try {
        if (tried == 1) {
          LOGGER.info("driver.get started");
          driver.get(playUrl);
          LOGGER.info("checking log...");
          checkLog(driver, urls, srt);
          LOGGER.info("driver.get finished");
        }
      } catch (Exception ex) {
        LOGGER.info("timeout exceeded. tried = " + tried);
        checkLog(driver, urls, srt);
        if (getSourceFromSet(urls) == null) {
          retry(driver);
          checkLog(driver, urls, srt);
        }
      } finally {
        tried++;
      }
    }
    LOGGER.info("Found urls: " + urls);
    result.put("source", getSourceFromSet(urls));
    result.put("subTitle", srt.isEmpty() ? null : srt.stream().findFirst().orElse(null));
    return result;
  }

  @Override
  public Map<String, String> crawVideoUrl(String original) throws Exception {
    LOGGER.info("waiting for free executor...");
    driverQueue.take();
    RemoteWebDriver driver = new RemoteWebDriver(new URL(remoteDriverUrl), desiredCapabilities);
    LOGGER.info("got executor");
    Map<String, String> result = new HashMap<>();
    Set<String> urls = new HashSet<>();
    Set<String> srt = new HashSet<>();
    int tried = 1;
    while (getSourceFromSet(urls) == null) {
      try {
        if (tried > 6) {
          break;
        } else {
          if (tried > 1) {
            LOGGER.info("sleeping for waiting video ready");
            Thread.sleep(10000);
          }
        }
        LOGGER.info("driver.get started");
        driver.get(original);
        LOGGER.info("driver.get finished");
        LOGGER.info("checking log...");
        checkLog(driver, urls, srt);
      } catch (Exception ex) {
        LOGGER.info("timeout exceeded. tried = " + tried);
        checkLog(driver, urls, srt);
        if (getSourceFromSet(urls) == null) {
          retry(driver);
          checkLog(driver, urls, srt);
        }
      } finally {
        tried++;
      }
    }
    try {
      driver.close();
      driver.quit();
    } catch (Exception ignored) {

    }
    driverQueue.add(1);
    LOGGER.info("Found urls: " + urls);
    result.put("source", getSourceFromSet(urls));
    result.put("subTitle", srt.isEmpty() ? null : srt.stream().findFirst().orElse(null));
    return result;
  }

  private void retry(RemoteWebDriver driver) {
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
          sleep(1000);
        } catch (Exception ignored) {
          LOGGER.warn("ignore thread interrupt exception");
        }
      }
    }
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

  @Override
  public List<CrawDTO> crawVideoSourceWithPool(List<CrawDTO> crawDTOList) {
    crawDTOList.forEach(craw -> {
      try {
        Map<String, String> crawResult = this.crawVideoUrl(craw.getInput());
        craw.setResult(crawResult.get("source"));
        craw.setSubTitle(crawResult.get("subTitle"));
      } catch (Exception e) {
        e.printStackTrace();
        craw.setError(e.getMessage());
      }
    });

    return crawDTOList;
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
