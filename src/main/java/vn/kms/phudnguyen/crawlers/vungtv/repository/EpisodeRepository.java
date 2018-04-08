package vn.kms.phudnguyen.crawlers.vungtv.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Episode;
import vn.kms.phudnguyen.crawlers.vungtv.entity.Movie;

public interface EpisodeRepository extends PagingAndSortingRepository<Episode, String> {
}
