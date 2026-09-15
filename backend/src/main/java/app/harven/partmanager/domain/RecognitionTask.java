package app.harven.partmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recognition_tasks")
public class RecognitionTask {

    @Id
    private String id;

    private String photoId;
    private String originalFilename;
    private String contentType;

    @Builder.Default
    private RecognitionStatus status = RecognitionStatus.PENDING;

    @Builder.Default
    private Map<String, Object> aiResult = new HashMap<>();
    private String rawText;
    private Double confidence;

    private String errorMessage;
    private Long processingTimeMs;

    @CreatedDate
    private Instant createdAt;

    private Instant completedAt;
}
