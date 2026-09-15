package app.harven.partmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseEntity {
    private String name;
    private String type;
    private String manufacturer;
    private String partNumber;
    private String packageType;
    private String mounting;
    private int quantity;
    private String description;
    private Map<String, Object> metadata;
    private double confidence;
    private String rawText;
}
