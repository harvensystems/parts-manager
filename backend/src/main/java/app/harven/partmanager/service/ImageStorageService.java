package app.harven.partmanager.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final ReactiveGridFsTemplate gridFsTemplate;

    public Mono<String> storeImage(FilePart filePart) {
        String filename = filePart.filename() != null && !filePart.filename().isEmpty() ? filePart.filename() : "image.jpg";
        String contentType = filePart.headers().getContentType() != null ? filePart.headers().getContentType().toString() : "image/jpeg";
        return gridFsTemplate.store(filePart.content(), filename, contentType)
                .map(ObjectId::toHexString);
    }

    public Mono<String> storeImage(InputStream inputStream, String filename, String contentType) {
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readInputStream(
                () -> inputStream,
                DefaultDataBufferFactory.sharedInstance,
                4096
        );
        return gridFsTemplate.store(
                dataBufferFlux,
                filename != null ? filename : "image.jpg",
                contentType != null ? contentType : "image/jpeg"
        ).map(ObjectId::toHexString);
    }

    public Mono<ReactiveGridFsResource> getImageResource(String imageId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(imageId)))
                .flatMap(gridFsTemplate::getResource);
    }

    public Mono<Void> deleteImage(String imageId) {
        return gridFsTemplate.delete(new Query(Criteria.where("_id").is(imageId)));
    }
}
