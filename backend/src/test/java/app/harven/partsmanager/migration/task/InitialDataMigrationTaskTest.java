package app.harven.partsmanager.migration.task;

import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.domain.Part;
import app.harven.partsmanager.repository.AppSettingRepository;
import app.harven.partsmanager.repository.PartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialDataMigrationTaskTest {

    @Mock
    private AppSettingRepository appSettingRepository;

    @Mock
    private PartRepository partRepository;

    private InitialDataMigrationTask task;

    @BeforeEach
    void setUp() {
        task = new InitialDataMigrationTask(appSettingRepository, partRepository);
    }

    @Test
    void execute_createsSettingsAndDemoPartWhenEmpty() {
        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.empty());
        when(appSettingRepository.save(any(AppSetting.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        when(partRepository.count()).thenReturn(Mono.just(0L));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(task.execute())
                .verifyComplete();

        ArgumentCaptor<AppSetting> settingCaptor = ArgumentCaptor.forClass(AppSetting.class);
        verify(appSettingRepository).save(settingCaptor.capture());
        AppSetting savedSetting = settingCaptor.getValue();
        assertThat(savedSetting.getId()).isEqualTo("default_settings");
        assertThat(savedSetting.getLowStockThreshold()).isEqualTo(5);
        assertThat(savedSetting.getImageQuality()).isEqualTo(80);

        ArgumentCaptor<Part> partCaptor = ArgumentCaptor.forClass(Part.class);
        verify(partRepository).save(partCaptor.capture());
        Part savedPart = partCaptor.getValue();
        assertThat(savedPart.getPartNumber()).isEqualTo("STM32F103C8T6");
        assertThat(savedPart.getName()).contains("STM32F103C8T6");
        assertThat(savedPart.getQuantity()).isEqualTo(10);
    }

    @Test
    void execute_doesNotOverwriteExistingSettingsOrParts() {
        AppSetting existingSetting = AppSetting.builder().id("default_settings").imageQuality(95).build();
        when(appSettingRepository.findById("default_settings")).thenReturn(Mono.just(existingSetting));
        when(partRepository.count()).thenReturn(Mono.just(5L));

        StepVerifier.create(task.execute())
                .verifyComplete();

        verify(appSettingRepository, never()).save(any(AppSetting.class));
        verify(partRepository, never()).save(any(Part.class));
    }
}
