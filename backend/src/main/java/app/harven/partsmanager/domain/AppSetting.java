package app.harven.partsmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "app_settings")
public class AppSetting {

    @Id
    @Builder.Default
    private String id = "default_settings";

    @Builder.Default
    private Integer lowStockThreshold = 5;

    @Builder.Default
    private Integer imageQuality = 80;

    @Builder.Default
    private Integer defaultPageSize = 20;

    @Builder.Default
    private Boolean autoProcessAi = true;

    @Builder.Default
    private String aiProvider = "gemini";

    @Builder.Default
    private String customApiKey = "";

    @LastModifiedDate
    private Instant updatedAt;
}
