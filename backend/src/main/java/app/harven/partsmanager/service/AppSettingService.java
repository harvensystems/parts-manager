package app.harven.partsmanager.service;

import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.mapper.DtoMapper;
import app.harven.partsmanager.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AppSettingService {

    private static final String DEFAULT_SETTINGS_ID = "default_settings";
    private final AppSettingRepository appSettingRepository;
    private final DtoMapper dtoMapper;

    public Mono<AppSettingDto> getSettings() {
        return appSettingRepository.findById(DEFAULT_SETTINGS_ID)
                .defaultIfEmpty(AppSetting.builder()
                        .id(DEFAULT_SETTINGS_ID)
                        .lowStockThreshold(5)
                        .imageQuality(80)
                        .defaultPageSize(20)
                        .autoProcessAi(true)
                        .aiProvider("gemini")
                        .customApiKey("")
                        .updatedAt(Instant.now())
                        .build())
                .map(dtoMapper::toAppSettingDto);
    }

    public Mono<AppSettingDto> updateSettings(AppSettingDto dto) {
        return appSettingRepository.findById(DEFAULT_SETTINGS_ID)
                .defaultIfEmpty(AppSetting.builder().id(DEFAULT_SETTINGS_ID).build())
                .flatMap(setting -> {
                    if (dto.getLowStockThreshold() != null) {
                        setting.setLowStockThreshold(dto.getLowStockThreshold());
                    }
                    if (dto.getImageQuality() != null) {
                        setting.setImageQuality(dto.getImageQuality());
                    }
                    if (dto.getDefaultPageSize() != null) {
                        setting.setDefaultPageSize(dto.getDefaultPageSize());
                    }
                    if (dto.getAutoProcessAi() != null) {
                        setting.setAutoProcessAi(dto.getAutoProcessAi());
                    }
                    if (dto.getAiProvider() != null) {
                        setting.setAiProvider(dto.getAiProvider());
                    }
                    if (dto.getCustomApiKey() != null) {
                        setting.setCustomApiKey(dto.getCustomApiKey());
                    }
                    setting.setUpdatedAt(Instant.now());
                    return appSettingRepository.save(setting);
                })
                .map(dtoMapper::toAppSettingDto);
    }
}
