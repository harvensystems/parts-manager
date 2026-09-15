package app.harven.partsmanager.dto;

import app.harven.partsmanager.domain.RecognitionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionTaskResponseDto {
    private String id;
    private String photoId;
    private String originalFilename;
    private String contentType;
    private RecognitionStatus status;
    @Builder.Default
    private Map<String, Object> aiResult = new HashMap<>();
    private String rawText;
    private Double confidence;
    private String errorMessage;
    private Long processingTimeMs;
    private Instant createdAt;
    private Instant completedAt;
}
