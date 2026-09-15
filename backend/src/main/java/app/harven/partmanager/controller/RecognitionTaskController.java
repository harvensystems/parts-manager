package app.harven.partmanager.controller;

import app.harven.partmanager.dto.DataUrlUploadDto;
import app.harven.partmanager.dto.RecognitionTaskResponseDto;
import app.harven.partmanager.service.RecognitionTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/recognition-tasks")
@Tag(name = "Recognition", description = "AI component recognition queue and task processing")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RecognitionTaskController {

    private final RecognitionTaskService taskService;

    @GetMapping
    @Operation(summary = "Get all recognition tasks (queue)")
    public Flux<RecognitionTaskResponseDto> listTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recognition task status by ID")
    public Mono<ResponseEntity<RecognitionTaskResponseDto>> getTask(@Parameter(description = "Task ID") @PathVariable("id") String id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Upload image file as multipart/form-data for AI recognition")
    public Mono<RecognitionTaskResponseDto> uploadFile(@Parameter(description = "Image file (JPEG/PNG/WEBP)") @RequestPart("file") FilePart file) {
        return taskService.createTaskFromUpload(file);
    }

    @PostMapping("/data-url")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Upload Base64/DataURL image (e.g. from camera capture) for AI recognition")
    public Mono<RecognitionTaskResponseDto> uploadDataUrl(@RequestBody DataUrlUploadDto dto) {
        return taskService.createTaskFromDataUrl(dto.getDataUrl(), dto.getFilename());
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry AI recognition for a task")
    public Mono<ResponseEntity<RecognitionTaskResponseDto>> retryTask(@Parameter(description = "Task ID") @PathVariable("id") String id) {
        return taskService.retryTask(id)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recognition task and its associated image")
    public Mono<ResponseEntity<Void>> deleteTask(@Parameter(description = "Task ID") @PathVariable("id") String id) {
        return taskService.deleteTask(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
