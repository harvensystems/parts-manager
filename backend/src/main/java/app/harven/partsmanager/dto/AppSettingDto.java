package app.harven.partsmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System application settings DTO")
public class AppSettingDto {

    @Schema(description = "Low stock threshold alert quantity", example = "5")
    @Min(value = 0, message = "Minimum stock threshold cannot be negative")
    private Integer lowStockThreshold;

    @Schema(description = "Image compression quality (1-100)", example = "80")
    @Min(value = 1, message = "Image quality must be at least 1")
    @Max(value = 100, message = "Image quality cannot exceed 100")
    private Integer imageQuality;

    @Schema(description = "Default page size for parts catalog", example = "20")
    @Min(value = 5, message = "Page size must be at least 5")
    @Max(value = 200, message = "Page size cannot exceed 200")
    private Integer defaultPageSize;

    @Schema(description = "Auto process AI tasks upon upload", example = "true")
    private Boolean autoProcessAi;

    @Schema(description = "Active AI recognition provider", example = "gemini")
    private String aiProvider;

    @Schema(description = "Custom API key for AI provider", example = "sk-...")
    private String customApiKey;

    @Schema(description = "Timestamp of last settings update")
    private Instant updatedAt;
}
