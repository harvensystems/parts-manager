package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.Media;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MediaRepository extends ReactiveMongoRepository<Media, String> {
}
