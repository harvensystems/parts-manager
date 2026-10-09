package app.harven.partsmanager.migration;

import app.harven.partsmanager.domain.MigrationRecord;
import app.harven.partsmanager.repository.MigrationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService implements ApplicationRunner {

    private final List<MigrationTask> migrationTasks;
    private final MigrationRecordRepository migrationRecordRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting database migration runner...");
        try {
            runMigrations().block();
            log.info("Database migrations completed successfully.");
        } catch (Exception e) {
            log.error("Database migration runner failed: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    public Mono<Void> runMigrations() {
        if (migrationTasks == null || migrationTasks.isEmpty()) {
            log.info("No migration tasks found.");
            return Mono.empty();
        }

        List<MigrationTask> sortedTasks = migrationTasks.stream()
                .sorted(Comparator.comparingInt(MigrationTask::getOrder))
                .toList();

        return Flux.fromIterable(sortedTasks)
                .concatMap(this::runSingleMigration)
                .then();
    }

    private Mono<Void> runSingleMigration(MigrationTask task) {
        return migrationRecordRepository.existsById(task.getId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.info("Migration [{}] already executed. Skipping.", task.getId());
                        return Mono.empty();
                    }

                    log.info("Executing migration [{}] - '{}': {}", task.getId(), task.getName(), task.getDescription());
                    long startTime = System.currentTimeMillis();

                    return task.execute()
                            .then(Mono.defer(() -> {
                                long executionTime = System.currentTimeMillis() - startTime;
                                MigrationRecord record = MigrationRecord.builder()
                                        .id(task.getId())
                                        .name(task.getName())
                                        .description(task.getDescription())
                                        .order(task.getOrder())
                                        .executedAt(Instant.now())
                                        .executionTimeMs(executionTime)
                                        .build();
                                return migrationRecordRepository.save(record);
                            }))
                            .doOnSuccess(saved -> log.info("Migration [{}] successfully executed and recorded in {} ms",
                                    task.getId(), saved.getExecutionTimeMs()))
                            .doOnError(err -> log.error("Migration [{}] failed with error: {}",
                                    task.getId(), err.getMessage(), err))
                            .then();
                });
    }
}
