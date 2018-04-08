package vn.kms.phudnguyen.crawlers.vungtv.service;

import java.net.MalformedURLException;

public interface CrawlingService {
  void crawMovies();

  void crawEpisodes() throws MalformedURLException;

  String getMovieSource(String original);
}
