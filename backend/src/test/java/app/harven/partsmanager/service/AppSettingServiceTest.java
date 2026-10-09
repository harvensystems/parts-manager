package app.harven.partsmanager.service;

import app.harven.partsmanager.config.AiConfig;
import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.mapper.DtoMapper;
import app.harven.partsmanager.repository.AppSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingServiceTest {

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private AiConfig aiConfig;

    private DtoMapper dtoMapper;
    private AppSettingService appSettingService;

    @BeforeEach
    void setUp() {
        dtoMapper = new DtoMapper();
        appSettingService = new AppSettingService(appSettingRepository, dtoMapper, null, aiConfig);
    }

    @Test
    void getSettings_whenExists_returnsMappedDto() {
        AppSetting existing = AppSetting.builder()
                .id("default_settings")
                .lowStockThreshold(10)
                .imageQuality(90)
                .defaultPageSize(50)
                .autoProcessAi(true)
                .aiProvider("openai")
                .customApiKey("sk-key-123")
                .updatedAt(Instant.now())
                .build();

        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.just(existing));

        StepVerifier.create(appSettingService.getSettings())
                .assertNext(dto -> {
                    assertEquals(10, dto.getLowStockThreshold());
                    assertEquals("openai", dto.getAiProvider());
                    assertEquals("sk-key-123", dto.getCustomApiKey());
                })
                .verifyComplete();
    }

    @Test
    void getSettings_whenNotExists_savesAndReturnsDefaults() {
        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.empty());
        when(appSettingRepository.save(any(AppSetting.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(appSettingService.getSettings())
                .assertNext(dto -> {
                    assertEquals(5, dto.getLowStockThreshold());
                    assertEquals("gemini", dto.getAiProvider());
                    assertEquals("", dto.getCustomApiKey());
                })
                .verifyComplete();
    }

    @Test
    void updateSettings_withProviderAndApiKey_recreatesChatClientBean() {
        AppSetting existing = AppSetting.builder()
                .id("default_settings")
                .lowStockThreshold(5)
                .imageQuality(80)
                .defaultPageSize(20)
                .autoProcessAi(true)
                .aiProvider("gemini")
                .customApiKey("")
                .updatedAt(Instant.now())
                .build();

        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.just(existing));
        when(appSettingRepository.save(any(AppSetting.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        AppSettingDto updateDto = AppSettingDto.builder()
                .aiProvider("gemini")
                .customApiKey("AIzaSyNewKey123")
                .build();

        StepVerifier.create(appSettingService.updateSettings(updateDto))
                .assertNext(dto -> {
                    assertEquals("gemini", dto.getAiProvider());
                    assertEquals("AIzaSyNewKey123", dto.getCustomApiKey());
                })
                .verifyComplete();

        verify(aiConfig, times(1)).recreateChatClientBean("gemini", "AIzaSyNewKey123");
    }

    @Test
    void updateSettings_withoutApiKey_doesNotRecreateChatClientBean() {
        AppSetting existing = AppSetting.builder()
                .id("default_settings")
                .lowStockThreshold(5)
                .imageQuality(80)
                .defaultPageSize(20)
                .autoProcessAi(true)
                .aiProvider("gemini")
                .customApiKey("")
                .updatedAt(Instant.now())
                .build();

        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.just(existing));
        when(appSettingRepository.save(any(AppSetting.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        AppSettingDto updateDto = AppSettingDto.builder()
                .lowStockThreshold(15)
                .build();

        StepVerifier.create(appSettingService.updateSettings(updateDto))
                .assertNext(dto -> {
                    assertEquals(15, dto.getLowStockThreshold());
                    assertEquals("", dto.getCustomApiKey());
                })
                .verifyComplete();

        verify(aiConfig, never()).recreateChatClientBean(any(), any());
    }

    @Test
    void updateSettings_withBlankApiKey_doesNotRecreateChatClientBean() {
        AppSetting existing = AppSetting.builder()
                .id("default_settings")
                .lowStockThreshold(5)
                .aiProvider("gemini")
                .customApiKey("oldKey")
                .build();

        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.just(existing));
        when(appSettingRepository.save(any(AppSetting.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        AppSettingDto updateDto = AppSettingDto.builder()
                .customApiKey("   ")
                .build();

        StepVerifier.create(appSettingService.updateSettings(updateDto))
                .assertNext(dto -> {
                    assertEquals("   ", dto.getCustomApiKey());
                })
                .verifyComplete();

        verify(aiConfig, never()).recreateChatClientBean(any(), any());
    }
}
