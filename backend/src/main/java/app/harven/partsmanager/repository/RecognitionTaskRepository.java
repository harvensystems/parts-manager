package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.RecognitionStatus;
import app.harven.partsmanager.domain.RecognitionTask;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecognitionTaskRepository extends ReactiveMongoRepository<RecognitionTask, String> {
    Flux<RecognitionTask> findByStatus(RecognitionStatus status);
}
