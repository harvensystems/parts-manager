package app.harven.partsmanager.controller;

import app.harven.partsmanager.dto.ImageUploadResponseDto;
import app.harven.partsmanager.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
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
    @Operation(summary = "Get binary image by ID", responses = @ApiResponse(responseCode = "200", content = @Content(mediaType = "image/jpeg")))
    Mono<Void> getImage(@Parameter(description = "Image ID in GridFS")
                                                 @PathVariable("id") String id, ServerWebExchange exchange) {

        return imageStorageService.getImageResource(id)
                .flatMap(resource -> {
                    MediaType mediaType = MediaType.IMAGE_JPEG;
                    return resource.getGridFSFile()
                            .flatMap(file -> {
                                exchange.getResponse().getHeaders().setContentType(mediaType);
                                return exchange.getResponse().writeWith(resource.getContent());
                    });
                });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete image by ID")
    public Mono<ResponseEntity<Void>> deleteImage(@Parameter(description = "Image ID in GridFS") @PathVariable("id") String id) {
        return imageStorageService.deleteImage(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
