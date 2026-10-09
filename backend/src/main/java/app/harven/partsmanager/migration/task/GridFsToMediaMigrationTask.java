package app.harven.partsmanager.migration.task;

import app.harven.partsmanager.domain.Media;
import app.harven.partsmanager.migration.MigrationTask;
import app.harven.partsmanager.repository.MediaRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GridFsToMediaMigrationTask implements MigrationTask {

    private static final String TASK_ID = "002_gridfs_to_media_migration";

    private final ReactiveGridFsTemplate gridFsTemplate;
    private final MediaRepository mediaRepository;

    @Override
    public String getId() {
        return TASK_ID;
    }

    @Override
    public String getName() {
        return "GridFS to Media Collection Migration";
    }

    @Override
    public String getDescription() {
        return "Migrate all files from GridFS storage into media collection preserving original IDs";
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public Mono<Void> execute() {
        return gridFsTemplate.find(new Query())
                .flatMap(this::migrateGridFsFile)
                .then();
    }

    private Mono<Media> migrateGridFsFile(GridFSFile gridFSFile) {
        String fileId;
        if (gridFSFile.getObjectId() != null) {
            fileId = gridFSFile.getObjectId().toHexString();
        } else if (gridFSFile.getId() != null) {
            fileId = gridFSFile.getId().isObjectId()
                    ? gridFSFile.getId().asObjectId().getValue().toHexString()
                    : gridFSFile.getId().toString();
        } else {
            fileId = null;
        }

        if (fileId == null) {
            log.warn("Skipping GridFS file with null ID: {}", gridFSFile.getFilename());
            return Mono.empty();
        }

        return gridFsTemplate.getResource(gridFSFile)
                .flatMap(resource -> DataBufferUtils.join(resource.getDownloadStream())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return bytes;
                        })
                        .defaultIfEmpty(new byte[0])
                )
                .flatMap(fileBytes -> {
                    String fileName = gridFSFile.getFilename() != null && !gridFSFile.getFilename().isBlank()
                            ? gridFSFile.getFilename()
                            : "image.jpg";

                    String contentType = "image/jpeg";
                    Map<String, Object> metaMap = new HashMap<>();
                    Document metadata = gridFSFile.getMetadata();
                    if (metadata != null) {
                        metaMap.putAll(metadata);
                        if (metadata.containsKey("_contentType") && metadata.get("_contentType") != null) {
                            contentType = metadata.getString("_contentType");
                        } else if (metadata.containsKey("contentType") && metadata.get("contentType") != null) {
                            contentType = metadata.getString("contentType");
                        }
                    }

                    Instant createdAt = gridFSFile.getUploadDate() != null
                            ? gridFSFile.getUploadDate().toInstant()
                            : Instant.now();

                    Media media = Media.builder()
                            .id(fileId)
                            .fileName(fileName)
                            .contentType(contentType)
                            .size(fileBytes.length)
                            .data(fileBytes)
                            .metadata(metaMap)
                            .createdAt(createdAt)
                            .updatedAt(Instant.now())
                            .build();

                    log.info("Migrating GridFS file [{}] id={}, size={} bytes to media collection", fileName, fileId, fileBytes.length);
                    return mediaRepository.save(media);
                });
    }
}
