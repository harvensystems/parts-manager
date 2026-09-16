package app.harven.partsmanager.controller;

import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.service.AppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Settings", description = "Application and AI recognition settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping
    @Operation(summary = "Get application settings")
    public Mono<AppSettingDto> getSettings() {
        return appSettingService.getSettings();
    }

    @PutMapping
    @Operation(summary = "Update application settings")
    public Mono<AppSettingDto> updateSettings(@Valid @RequestBody AppSettingDto dto) {
        return appSettingService.updateSettings(dto);
    }
}
