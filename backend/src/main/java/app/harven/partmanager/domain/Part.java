package app.harven.partmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "parts")
public class Part {

    @Id
    private String id;

    @TextIndexed(weight = 3)
    private String name;

    @TextIndexed(weight = 2)
    private String type;

    @TextIndexed
    private String manufacturer;

    @TextIndexed(weight = 3)
    private String partNumber;

    @TextIndexed
    private String packageType;

    @TextIndexed
    private String mounting;

    @Builder.Default
    private Integer quantity = 0;

    @TextIndexed
    private String description;

    @Builder.Default
    private List<String> photoIds = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
