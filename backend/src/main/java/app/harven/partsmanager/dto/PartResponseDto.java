package app.harven.partsmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartResponseDto {
    private String id;
    private String name;
    private String type;
    private String manufacturer;
    private String partNumber;
    private String packageType;
    private String mounting;
    private Integer quantity;
    private String description;
    @Builder.Default
    private List<String> photoIds = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private Instant createdAt;
    private Instant updatedAt;
}
