package vn.kms.phudnguyen.crawlers.vungtv.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;
import vn.kms.phudnguyen.crawlers.vungtv.dto.TaskStatus;
import vn.kms.phudnguyen.crawlers.vungtv.dto.TaskType;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Task;
import vn.kms.phudnguyen.crawlers.vungtv.repository.TaskRepository;
import vn.kms.phudnguyen.crawlers.vungtv.service.CrawlingService;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/craw")
public class CrawlingController {

  @Autowired
  private CrawlingService crawlingService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private Gson gson;

  @PostMapping
  public List<CrawDTO> craw(@RequestBody String body) throws MalformedURLException {
    JsonArray array = gson.fromJson(body, JsonArray.class);
    List<CrawDTO> dtos = new ArrayList<>(array.size());
    for (int i = 0; i < array.size(); ++i) {
      JsonObject item = array.get(i).getAsJsonObject();
      dtos.add(CrawDTO.builder()
          .id(item.get("id").getAsString())
          .input(item.get("input").getAsString())
          .build());
    }
    return crawlingService.crawVideoSource(dtos);
  }

  @GetMapping("/movie/{id}")
  public Task crawMovie(@PathVariable String id) {
    Task task = Task.builder()
        .status(TaskStatus.NOT_START)
        .target(id)
        .type(TaskType.CRAW_MOVIE)
        .build();
    taskRepository.save(task);
    return task;
  }

  @GetMapping("/serie/{id}")
  public Task crawSerie(@PathVariable String id) {
    Task task = Task.builder()
        .status(TaskStatus.NOT_START)
        .target(id)
        .type(TaskType.CRAW_SERIE)
        .build();
    taskRepository.save(task);
    return task;
  }

  @GetMapping("/episode/{id}")
  public Task crawEpisode(@PathVariable String id) {
    Task task = Task.builder()
        .status(TaskStatus.NOT_START)
        .target(id)
        .type(TaskType.CRAW_EPISODE)
        .build();
    taskRepository.save(task);
    return task;
  }

}
