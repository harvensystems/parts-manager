package app.harven.partsmanager.controller;

import app.harven.partsmanager.dto.AdjustQuantityDto;
import app.harven.partsmanager.dto.CreateOrUpdatePartDto;
import app.harven.partsmanager.dto.PartResponseDto;
import app.harven.partsmanager.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/parts")
@Tag(name = "Parts", description = "Workshop components and stock management")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @GetMapping
    @Operation(summary = "List components with optional search and filters")
    public Flux<PartResponseDto> listParts(
            @Parameter(description = "Search query (name, part number, package, metadata)") @RequestParam(name = "search", required = false) String search,
            @Parameter(description = "Component type filter (e.g. Resistor, IC)") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "Mounting filter (SMD or Through-hole)") @RequestParam(name = "mounting", required = false) String mounting,
            @Parameter(description = "Sort property (updatedAt, name, quantity)") @RequestParam(name = "sortBy", required = false, defaultValue = "updatedAt") String sortBy
    ) {
        return partService.findAllList(search, type, mounting, sortBy);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated components list")
    public Mono<Page<PartResponseDto>> pagedParts(
            @Parameter(description = "Search query") @RequestParam(name = "search", required = false) String search,
            @Parameter(description = "Component type filter") @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "Mounting filter") @RequestParam(name = "mounting", required = false) String mounting,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(name = "size", required = false, defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(name = "sortBy", required = false, defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)") @RequestParam(name = "direction", required = false, defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return partService.findAll(search, type, mounting, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get component by ID")
    public Mono<ResponseEntity<PartResponseDto>> getPartById(@Parameter(description = "Part ID") @PathVariable("id") String id) {
        return partService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create or update component (automatically increments quantity if Part Number matches)")
    public Mono<PartResponseDto> saveOrUpdatePart(@Valid @RequestBody CreateOrUpdatePartDto dto) {
        return partService.saveOrUpdate(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update component by ID")
    public Mono<ResponseEntity<PartResponseDto>> updatePart(@Parameter(description = "Part ID") @PathVariable("id") String id, @Valid @RequestBody CreateOrUpdatePartDto dto) {
        return partService.updatePart(id, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/quantity")
    @Operation(summary = "Adjust component stock quantity (increment or decrement)")
    public Mono<ResponseEntity<PartResponseDto>> adjustQuantity(@Parameter(description = "Part ID") @PathVariable("id") String id, @RequestBody AdjustQuantityDto dto) {
        int delta = dto.getDelta() != null ? dto.getDelta() : 0;
        return partService.adjustQuantity(id, delta)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete component by ID")
    public Mono<ResponseEntity<Void>> deletePart(@Parameter(description = "Part ID") @PathVariable("id") String id) {
        return partService.deleteById(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
