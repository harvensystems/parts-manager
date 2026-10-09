package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.MigrationRecord;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MigrationRecordRepository extends ReactiveMongoRepository<MigrationRecord, String> {
}
