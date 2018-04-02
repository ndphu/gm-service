package vn.kms.phudnguyen.crawlers.vungtv.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "movies")
@Builder
@Data
public class Movie {
  String id;
  String title;
  String poster;
  String bigPoster;
  String playUrl;
  String source;
  String videoSource;
  String content;
  String releaseDate;
  List<String> directors;
  List<String> actors;
  List<String> categories;
  List<String> countries;
}
