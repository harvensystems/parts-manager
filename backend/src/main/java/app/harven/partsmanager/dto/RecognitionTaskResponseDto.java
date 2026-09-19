package app.harven.partsmanager.dto;

import app.harven.partsmanager.domain.RecognitionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private PartResponseDto part;
    private String rawText;
    private Double confidence;
    private String errorMessage;
    private Long processingTimeMs;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @Schema(type = "integer")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @Schema(type = "integer")
    private Instant completedAt;
}
