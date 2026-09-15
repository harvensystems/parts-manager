package app.harven.partmanager.service;

import app.harven.partmanager.domain.RecognitionStatus;
import app.harven.partmanager.domain.RecognitionTask;
import app.harven.partmanager.dto.RecognitionTaskResponseDto;
import app.harven.partmanager.mapper.DtoMapper;
import app.harven.partmanager.repository.RecognitionTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RecognitionTaskService {

    private final RecognitionTaskRepository taskRepository;
    private final ImageStorageService imageStorageService;
    private final AiRecognitionService aiRecognitionService;
    private final DtoMapper dtoMapper;

    public Mono<RecognitionTaskResponseDto> createTaskFromUpload(FilePart file) {
        String filename = file.filename() != null && !file.filename().isEmpty() ? file.filename() : "upload.jpg";
        String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString() : "image/jpeg";

        return imageStorageService.storeImage(file)
                .flatMap(photoId -> {
                    RecognitionTask task = RecognitionTask.builder()
                            .photoId(photoId)
                            .originalFilename(filename)
                            .contentType(contentType)
                            .status(RecognitionStatus.PENDING)
                            .createdAt(Instant.now())
                            .build();

                    return taskRepository.save(task)
                            .doOnSuccess(savedTask -> aiRecognitionService.processTaskAsync(savedTask.getId()))
                            .map(dtoMapper::toRecognitionTaskResponseDto);
                });
    }

    public Mono<RecognitionTaskResponseDto> createTaskFromDataUrl(String dataUrl, String filename) {
        byte[] imageBytes;
        String contentType = "image/jpeg";

        if (dataUrl != null && dataUrl.startsWith("data:")) {
            String[] parts = dataUrl.split(",");
            String metadataPart = parts[0];
            String base64Data = parts.length > 1 ? parts[1] : "";
            if (metadataPart.contains("image/png")) {
                contentType = "image/png";
            } else if (metadataPart.contains("image/webp")) {
                contentType = "image/webp";
            }
            imageBytes = Base64.getDecoder().decode(base64Data);
        } else if (dataUrl != null) {
            imageBytes = dataUrl.getBytes(StandardCharsets.UTF_8);
        } else {
            imageBytes = new byte[0];
        }

        InputStream is = new ByteArrayInputStream(imageBytes);
        final String finalContentType = contentType;
        final String finalFilename = filename != null ? filename : "upload.jpg";

        return imageStorageService.storeImage(is, finalFilename, finalContentType)
                .flatMap(photoId -> {
                    RecognitionTask task = RecognitionTask.builder()
                            .photoId(photoId)
                            .originalFilename(finalFilename)
                            .contentType(finalContentType)
                            .status(RecognitionStatus.PENDING)
                            .createdAt(Instant.now())
                            .build();

                    return taskRepository.save(task)
                            .doOnSuccess(savedTask -> aiRecognitionService.processTaskAsync(savedTask.getId()))
                            .map(dtoMapper::toRecognitionTaskResponseDto);
                });
    }

    public Flux<RecognitionTaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .sort(Comparator.comparing(RecognitionTask::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(dtoMapper::toRecognitionTaskResponseDto);
    }

    public Mono<RecognitionTaskResponseDto> getTaskById(String id) {
        return taskRepository.findById(id)
                .map(dtoMapper::toRecognitionTaskResponseDto);
    }

    public Mono<RecognitionTaskResponseDto> retryTask(String id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Task not found: " + id)))
                .flatMap(task -> {
                    task.setStatus(RecognitionStatus.PENDING);
                    task.setErrorMessage(null);
                    return taskRepository.save(task)
                            .doOnSuccess(savedTask -> aiRecognitionService.processTaskAsync(savedTask.getId()))
                            .map(dtoMapper::toRecognitionTaskResponseDto);
                });
    }

    public Mono<Void> deleteTask(String id) {
        return taskRepository.findById(id)
                .flatMap(task -> {
                    Mono<Void> deleteImg = task.getPhotoId() != null
                            ? imageStorageService.deleteImage(task.getPhotoId())
                            : Mono.empty();
                    return deleteImg.then(taskRepository.deleteById(id));
                });
    }
}
