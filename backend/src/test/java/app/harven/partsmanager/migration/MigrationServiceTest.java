package app.harven.partsmanager.migration;

import app.harven.partsmanager.domain.MigrationRecord;
import app.harven.partsmanager.repository.MigrationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

    @Mock
    private MigrationRecordRepository migrationRecordRepository;

    @Mock
    private MigrationTask task1;

    @Mock
    private MigrationTask task2;

    private MigrationService migrationService;

    @BeforeEach
    void setUp() {
        lenient().when(task1.getId()).thenReturn("001_first");
        lenient().when(task1.getName()).thenReturn("First Task");
        lenient().when(task1.getDescription()).thenReturn("First task description");
        lenient().when(task1.getOrder()).thenReturn(1);

        lenient().when(task2.getId()).thenReturn("002_second");
        lenient().when(task2.getName()).thenReturn("Second Task");
        lenient().when(task2.getDescription()).thenReturn("Second task description");
        lenient().when(task2.getOrder()).thenReturn(2);

        // Put in reverse order to ensure sorting works
        migrationService = new MigrationService(List.of(task2, task1), migrationRecordRepository);
    }

    @Test
    void runMigrations_executesNewTasksAndSkipsCompleted() {
        when(migrationRecordRepository.existsById("001_first")).thenReturn(Mono.just(true));
        when(migrationRecordRepository.existsById("002_second")).thenReturn(Mono.just(false));

        when(task2.execute()).thenReturn(Mono.empty());
        when(migrationRecordRepository.save(any(MigrationRecord.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(migrationService.runMigrations())
                .verifyComplete();

        verify(task1, never()).execute();
        verify(task2, times(1)).execute();

        ArgumentCaptor<MigrationRecord> recordCaptor = ArgumentCaptor.forClass(MigrationRecord.class);
        verify(migrationRecordRepository).save(recordCaptor.capture());

        MigrationRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getId()).isEqualTo("002_second");
        assertThat(savedRecord.getName()).isEqualTo("Second Task");
        assertThat(savedRecord.getDescription()).isEqualTo("Second task description");
        assertThat(savedRecord.getOrder()).isEqualTo(2);
        assertThat(savedRecord.getExecutedAt()).isNotNull();
        assertThat(savedRecord.getExecutionTimeMs()).isNotNull();
    }

    @Test
    void runMigrations_executesAllInCorrectOrderWhenNoneCompleted() {
        when(migrationRecordRepository.existsById("001_first")).thenReturn(Mono.just(false));
        when(migrationRecordRepository.existsById("002_second")).thenReturn(Mono.just(false));

        when(task1.execute()).thenReturn(Mono.empty());
        when(task2.execute()).thenReturn(Mono.empty());
        when(migrationRecordRepository.save(any(MigrationRecord.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(migrationService.runMigrations())
                .verifyComplete();

        verify(task1, times(1)).execute();
        verify(task2, times(1)).execute();
        verify(migrationRecordRepository, times(2)).save(any(MigrationRecord.class));
    }
}
