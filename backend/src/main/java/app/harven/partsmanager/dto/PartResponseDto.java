package app.harven.partsmanager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private String partCode;
    private String location;
    private String packageType;
    private String mounting;
    private Integer quantity;
    private String description;
    @Builder.Default
    private List<String> photoIds = new ArrayList<>();
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @Schema(type = "integer")
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @Schema(type = "integer")
    private Instant updatedAt;
}
