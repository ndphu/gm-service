package vn.kms.phudnguyen.crawlers.vungtv.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MovieDTO {
  String id;
  String title;
  String content;
  String releaseDate;
  List<String> actors;
  List<String> categories;
  String poster;
  String videoSource;
}
