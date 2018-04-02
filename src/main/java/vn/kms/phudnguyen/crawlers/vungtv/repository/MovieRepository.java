package vn.kms.phudnguyen.crawlers.vungtv.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Movie;

public interface MovieRepository extends PagingAndSortingRepository<Movie, String> {
}
