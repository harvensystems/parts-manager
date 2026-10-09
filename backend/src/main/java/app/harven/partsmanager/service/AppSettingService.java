package app.harven.partsmanager.service;

import app.harven.partsmanager.config.AiConfig;
import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.mapper.DtoMapper;
import app.harven.partsmanager.repository.AppSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class AppSettingService {

    private static final String DEFAULT_SETTINGS_ID = "default_settings";
    private final AppSettingRepository appSettingRepository;
    private final DtoMapper dtoMapper;
    private final AiConfig aiConfig;
    private final ApplicationContext applicationContext;

    @Autowired
    public AppSettingService(AppSettingRepository appSettingRepository,
                             DtoMapper dtoMapper,
                             ApplicationContext applicationContext,
                             @Lazy @Autowired(required = false) AiConfig aiConfig) {
        this.appSettingRepository = appSettingRepository;
        this.dtoMapper = dtoMapper;
        this.aiConfig = aiConfig;
        this.applicationContext = applicationContext;
    }

    public Mono<AppSettingDto> getSettings() {
        return appSettingRepository.findById(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(Mono.defer(() -> appSettingRepository.save(AppSetting.builder()
                            .id(DEFAULT_SETTINGS_ID)
                            .lowStockThreshold(5)
                            .imageQuality(80)
                            .defaultPageSize(20)
                            .autoProcessAi(true)
                            .aiProvider("gemini")
                            .customApiKey("")
                            .updatedAt(Instant.now())
                            .build())))
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
                .doOnNext(savedSetting -> {
                    if (savedSetting.getAiProvider() != null && !savedSetting.getAiProvider().trim().isEmpty()
                            && savedSetting.getCustomApiKey() != null && !savedSetting.getCustomApiKey().trim().isEmpty()) {
                        if (aiConfig != null) {
                            aiConfig.recreateChatClientBean(savedSetting.getAiProvider(), savedSetting.getCustomApiKey());
                        }
                    }
                })
                .map(dtoMapper::toAppSettingDto);
    }

    Mono<Boolean> enabledAI() {
        return getSettings()
                .map(setting -> StringUtils.hasText(setting.getAiProvider()) && StringUtils.hasText(setting.getCustomApiKey()));
    }
}
