package app.harven.partsmanager.migration.task;

import app.harven.partsmanager.domain.Media;
import app.harven.partsmanager.repository.MediaRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GridFsToMediaMigrationTaskTest {

    @Mock
    private ReactiveGridFsTemplate gridFsTemplate;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private ReactiveGridFsResource resource;

    private GridFsToMediaMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new GridFsToMediaMigrationTask(gridFsTemplate, mediaRepository);
    }

    @Test
    void execute_migratesGridFsFilesToMediaCollection() {
        ObjectId objectId = new ObjectId();
        Document metadata = new Document();
        metadata.put("_contentType", "image/png");
        metadata.put("source", "camera");

        GridFSFile gridFSFile = new GridFSFile(
                new BsonObjectId(objectId),
                "test-image.png",
                12L,
                4096,
                new Date(),
                metadata
        );

        when(gridFsTemplate.find(any(Query.class))).thenReturn(Flux.just(gridFSFile));
        when(gridFsTemplate.getResource(gridFSFile)).thenReturn(Mono.just(resource));

        byte[] rawBytes = "Hello Buffer".getBytes();
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(rawBytes);
        when(resource.getDownloadStream()).thenReturn(Flux.just(dataBuffer));

        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(task.execute())
                .verifyComplete();

        ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(mediaCaptor.capture());

        Media savedMedia = mediaCaptor.getValue();
        assertThat(savedMedia.getId()).isEqualTo(objectId.toHexString());
        assertThat(savedMedia.getFileName()).isEqualTo("test-image.png");
        assertThat(savedMedia.getContentType()).isEqualTo("image/png");
        assertThat(savedMedia.getSize()).isEqualTo(rawBytes.length);
        assertThat(savedMedia.getData()).isEqualTo(rawBytes);
        assertThat(savedMedia.getMetadata()).containsEntry("source", "camera");
    }

    @Test
    void execute_handlesEmptyGridFs() {
        when(gridFsTemplate.find(any(Query.class))).thenReturn(Flux.empty());

        StepVerifier.create(task.execute())
                .verifyComplete();

        verify(mediaRepository, never()).save(any(Media.class));
    }
}
