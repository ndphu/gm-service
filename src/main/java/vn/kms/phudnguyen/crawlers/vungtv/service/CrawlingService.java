package vn.kms.phudnguyen.crawlers.vungtv.service;

import org.openqa.selenium.remote.RemoteWebDriver;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public interface CrawlingService {
  Map<String, String> getMovieSource(RemoteWebDriver driver, String original) throws IOException;

  Map<String, String> crawVideoUrl(String original) throws Exception;

  List<CrawDTO> crawVideoSource(List<CrawDTO> crawList) throws MalformedURLException;

  List<CrawDTO> crawVideoSourceWithPool(List<CrawDTO> crawDTOList) throws MalformedURLException;
}
