package app.harven.partsmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "migrations")
public class MigrationRecord {

    @Id
    private String id;

    private String name;

    private String description;

    private Integer order;

    private Instant executedAt;

    private Long executionTimeMs;
}
