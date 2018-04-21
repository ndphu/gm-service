package vn.kms.phudnguyen.crawlers.vungtv.config;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;

@Configuration
public class DriverConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(DriverConfig.class);

  @Value("${webdriver.config.headless.enabled:false}")
  private boolean headless;

  @Bean
  DesiredCapabilities desiredCapabilities() {
    DesiredCapabilities caps = DesiredCapabilities.chrome();
    LoggingPreferences loggingPreferences = new LoggingPreferences();
    loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);
    caps.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);

    if (headless) {
      LOGGER.info("headless enabled");
      ChromeOptions chromeOptions = new ChromeOptions();
      chromeOptions.setHeadless(headless);
      caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
    }

    return caps;
  }
}
