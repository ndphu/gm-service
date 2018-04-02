package vn.kms.phudnguyen.crawlers.vungtv.service;

import org.springframework.data.domain.Page;
import vn.kms.phudnguyen.crawlers.vungtv.dto.MovieDTO;

import java.util.List;

public interface MovieService {
  List<MovieDTO> findAll();

  Page<MovieDTO> findPaginated(int page, int size);

  MovieDTO findById(String id);
}
