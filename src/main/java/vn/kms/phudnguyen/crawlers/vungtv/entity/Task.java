package vn.kms.phudnguyen.crawlers.vungtv.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.kms.phudnguyen.crawlers.vungtv.dto.TaskStatus;
import vn.kms.phudnguyen.crawlers.vungtv.dto.TaskType;

@Data
@Builder
@Document(collection = "tasks")
public class Task {
  @Id
  String id;
  TaskType type;
  TaskStatus status;
  String output;
  String target;
}
