package app.harven.partsmanager.service;

import app.harven.partsmanager.domain.Media;
import app.harven.partsmanager.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bson.Document;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final MediaRepository mediaRepository;

    private Mono<byte[]> convertFilePartToByteArray(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .defaultIfEmpty(new byte[0]);
    }

    public Mono<String> storeImage(FilePart filePart) {
        String filename = filePart.filename() != null && !filePart.filename().isEmpty() ? filePart.filename() : "image.jpg";
        String contentType = filePart.headers().getContentType() != null ? filePart.headers().getContentType().toString() : "image/jpeg";
        return convertFilePartToByteArray(filePart)
                .flatMap(image -> {
                    Media media = Media.builder()
                            .contentType(contentType)
                            .size(image.length)
                            .data(image)
                            .fileName(filename)
                            .build();
                    return mediaRepository.save(media)
                            .map(Media::getId);
                });
    }

    public Mono<String> storeImage(InputStream inputStream, String filename, String contentType) {
        Media media = null;
        try {
            media = Media.builder()
                    .contentType(contentType != null ? contentType : "image/jpeg")
                    .fileName(filename != null ? filename : "image.jpg")
                    .data(inputStream.readAllBytes())
                    .size(inputStream.available())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return mediaRepository.save(media).map(Media::getId);
    }

    public Mono<byte[]> getImageBytes(String imageId) {
        return mediaRepository.findById(imageId)
                .map(Media::getData);
    }

    public Mono<Void> deleteImage(String imageId) {
        return mediaRepository.deleteById(imageId);
    }

    public byte[] normalizeImage(byte[] image, int width, int height, Float quality) {
        if (image == null || image.length == 0) {
            return image;
        }

        ByteArrayInputStream input = new ByteArrayInputStream(image);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {

            Thumbnails.of(input)
                    .size(width, height)             // max size
                    .outputFormat("jpg")             // format ("jpg", "png"...)
                    .outputQuality(quality != null ? quality : 0.85f)             // quality (from 0.0 to 1.0)
                    .toOutputStream(output);
            return output.toByteArray();
        } catch (IOException e) {
            log.error("Error while resizing image", e);
            return image;
        }
    }

    //TODO resave image with optimized quality
    public Mono<Void> optimizeImage(String imageId, int width, int height, Float quality) {
        return mediaRepository.findById(imageId)
                .flatMap(media -> {
                    byte[] normalizedImage = normalizeImage(media.getData(), width, height, quality);
                    media.setData(normalizedImage);
                    media.setSize(normalizedImage.length);
                    return mediaRepository.save(media);
                })
                .then();
    }

    public Mono<Void> optimizeImage(List<String> imageIds, int width, int height, Float quality) {
        return Flux.fromIterable(imageIds)
                .flatMap(imageId -> optimizeImage(imageId, width, height, quality))
                .then();
    }
}
