package vn.kms.phudnguyen.crawlers.vungtv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawDTO {
  String id;
  String input;
  String result;
}
