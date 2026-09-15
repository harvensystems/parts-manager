package app.harven.partsmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdatePartDto {

    @NotBlank
    private String name;

    private String type;
    private String manufacturer;
    private String partNumber;
    private String packageType;
    private String mounting;

    @NotNull
    @Builder.Default
    private Integer quantity = 1;

    private String description;
    @Builder.Default
    private List<String> photoIds = new ArrayList<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
