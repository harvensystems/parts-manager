package app.harven.partsmanager.service;

import app.harven.partsmanager.domain.RecognitionStatus;
import app.harven.partsmanager.domain.RecognitionTask;
import app.harven.partsmanager.repository.RecognitionTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRecognitionServiceTest {

    @Mock
    private RecognitionTaskRepository taskRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private PartService partService;

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    private AiRecognitionService aiRecognitionService;

    @BeforeEach
    void setUp() {
        aiRecognitionService = new AiRecognitionService(
                taskRepository,
                imageStorageService,
                appSettingService,
                partService,
                chatClientBuilderProvider
        );
    }

    @Test
    void processTaskAsync_whenAiDisabled_runsSimulatedProcess() throws InterruptedException {
        RecognitionTask task = RecognitionTask.builder()
                .id("task-1")
                .photoId("photo-1")
                .status(RecognitionStatus.PENDING)
                .build();

        when(taskRepository.findById("task-1")).thenReturn(Mono.just(task));
        when(taskRepository.save(any(RecognitionTask.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        aiRecognitionService.processTaskAsync("task-1");

        // Wait a short moment for async reactive pipeline to complete
        Thread.sleep(200);

        verify(taskRepository, atLeastOnce()).save(any(RecognitionTask.class));
    }
}
