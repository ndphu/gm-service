package vn.kms.phudnguyen.crawlers.vungtv.service;

import org.openqa.selenium.remote.RemoteWebDriver;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;

import java.net.MalformedURLException;
import java.util.List;

public interface CrawlingService {
  String getMovieSource(RemoteWebDriver driver, String original);

  List<CrawDTO> crawVideoSource(List<CrawDTO> crawList) throws MalformedURLException;
}
