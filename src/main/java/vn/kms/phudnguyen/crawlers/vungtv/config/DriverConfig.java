package vn.kms.phudnguyen.crawlers.vungtv.config;

import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;

@Configuration
public class DriverConfig {

  @Bean
  DesiredCapabilities desiredCapabilities() {
    DesiredCapabilities caps = DesiredCapabilities.chrome();
    LoggingPreferences loggingPreferences = new LoggingPreferences();
    loggingPreferences.enable(LogType.PERFORMANCE, Level.INFO);
    caps.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
    return caps;
  }
}
