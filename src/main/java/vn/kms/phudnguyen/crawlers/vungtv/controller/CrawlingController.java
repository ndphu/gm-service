package vn.kms.phudnguyen.crawlers.vungtv.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kms.phudnguyen.crawlers.vungtv.dto.CrawDTO;
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



}
