package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.Part;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PartRepository extends ReactiveMongoRepository<Part, String> {
    Flux<Part> findAllBy(TextCriteria criteria, Pageable pageable);
    Mono<Part> findByPartNumberIgnoreCase(String partNumber);
}
