package vn.kms.phudnguyen.crawlers.vungtv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import vn.kms.phudnguyen.crawlers.vungtv.dto.MovieDTO;
import vn.kms.phudnguyen.crawlers.vungtv.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/gm")
public class MovieController {

  @Autowired
  MovieService movieService;

  @GetMapping
  @RequestMapping(value = "/movie")
  @ResponseBody
  List<MovieDTO> findAll() {
    return movieService.findAll();
  }

  @GetMapping
  @RequestMapping(value = "/movie/{id}")
  @ResponseBody
  MovieDTO findById(@PathVariable String id) {
    return movieService.findById(id);
  }

  @GetMapping
  @RequestMapping(value = "/movie/paginated", params = { "page", "size" })
  @ResponseBody
  Page<MovieDTO> findAllPaginated(
      @RequestParam( "page" ) int page, @RequestParam( "size" ) int size
  ) {
    return movieService.findPaginated(page, size);
  }
}
