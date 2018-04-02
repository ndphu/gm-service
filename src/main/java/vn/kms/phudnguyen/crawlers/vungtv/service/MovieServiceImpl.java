package vn.kms.phudnguyen.crawlers.vungtv.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.kms.phudnguyen.crawlers.vungtv.dto.MovieDTO;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Movie;
import vn.kms.phudnguyen.crawlers.vungtv.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
  @Autowired
  MovieRepository movieRepository;

  @Override
  public List<MovieDTO> findAll() {
    ArrayList<Movie> movies = Lists.newArrayList(movieRepository.findAll());
    return movies.stream().map(MovieServiceImpl::mapMovie).collect(Collectors.toList());
  }

  @Override
  public Page<MovieDTO> findPaginated(int page, int size) {
    PageRequest pageable = PageRequest.of(page, size);
    Page<Movie> movies = movieRepository.findAll(pageable);
    Page<MovieDTO> dtos = new PageImpl<>(
        movies.getContent().stream().map(MovieServiceImpl::mapMovie).collect(Collectors.toList()),
        pageable,
        movies.getTotalElements()
    );
    return dtos;
  }

  @Override
  public MovieDTO findById(String id) {
    Movie m = movieRepository.findById(id).get();
    return mapMovie(m);
  }

  private static MovieDTO mapMovie(Movie m) {
    return MovieDTO.builder()
        .id(m.getId())
        .title(m.getTitle())
        .poster(m.getPoster())
        .content(m.getContent())
        .actors(m.getActors())
        .categories(m.getCategories())
        .releaseDate(m.getReleaseDate())
        .videoSource(m.getVideoSource())
        .build();
  }
}
