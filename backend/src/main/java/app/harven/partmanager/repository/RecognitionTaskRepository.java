package app.harven.partmanager.repository;

import app.harven.partmanager.domain.RecognitionStatus;
import app.harven.partmanager.domain.RecognitionTask;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RecognitionTaskRepository extends ReactiveMongoRepository<RecognitionTask, String> {
    Flux<RecognitionTask> findByStatus(RecognitionStatus status);
}
