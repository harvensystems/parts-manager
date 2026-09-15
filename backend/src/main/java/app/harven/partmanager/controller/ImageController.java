package app.harven.partmanager.controller;

import app.harven.partmanager.dto.ImageUploadResponseDto;
import app.harven.partmanager.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Component image storage and retrieval in GridFS")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image into GridFS")
    public Mono<ResponseEntity<ImageUploadResponseDto>> uploadImage(@Parameter(description = "Image file to upload") @RequestPart("file") FilePart file) {
        return imageStorageService.storeImage(file)
                .map(id -> {
                    ImageUploadResponseDto response = ImageUploadResponseDto.builder()
                            .id(id)
                            .url("/api/images/" + id)
                            .build();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                });
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get binary image by ID")
    public Mono<ResponseEntity<Object>> getImage(@Parameter(description = "Image ID in GridFS")
                                                   @PathVariable("id") String id) {

        return imageStorageService.getImageResource(id)
                .flatMap(resource -> {
                    MediaType mediaType = MediaType.IMAGE_JPEG;
                    return resource.getGridFSFile().map(file -> {
                        if (file.getMetadata().containsKey("_contentType")) {
//                            mediaType = MediaType.parseMediaType(file.getMetadata().getString("_contentType"));
                        }
                        return ResponseEntity.ok()
//                                .contentType(mediaType)
                                .body((Object) file);
                    });
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete image by ID")
    public Mono<ResponseEntity<Void>> deleteImage(@Parameter(description = "Image ID in GridFS") @PathVariable("id") String id) {
        return imageStorageService.deleteImage(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
