package vn.kms.phudnguyen.crawlers.vungtv.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "episodes")
@Builder
@Data
public class Episode {
  @Id
  String id;
  String title;
  String subTitle;
  int order;
  String videoSource;
  String crawUrl;
  String serieId;
}
