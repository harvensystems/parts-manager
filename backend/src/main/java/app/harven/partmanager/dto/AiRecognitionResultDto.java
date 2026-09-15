package app.harven.partmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecognitionResultDto {
    private String name;
    private String type;
    private String manufacturer;
    private String partNumber;
    private String packageType;
    private String mounting;
    private Integer quantity;
    private String description;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private Double confidence;
    private String rawText;
}
