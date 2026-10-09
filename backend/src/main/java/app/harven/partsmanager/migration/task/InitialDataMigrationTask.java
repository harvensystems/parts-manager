package app.harven.partsmanager.migration.task;

import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.domain.Part;
import app.harven.partsmanager.migration.MigrationTask;
import app.harven.partsmanager.repository.AppSettingRepository;
import app.harven.partsmanager.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataMigrationTask implements MigrationTask {

    private static final String TASK_ID = "001_initial_data_setup";
    private static final String DEFAULT_SETTINGS_ID = "default_settings";

    private final AppSettingRepository appSettingRepository;
    private final PartRepository partRepository;

    @Override
    public String getId() {
        return TASK_ID;
    }

    @Override
    public String getName() {
        return "Initial Data Setup";
    }

    @Override
    public String getDescription() {
        return "Initialize default application settings and a demo component if not present";
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public Mono<Void> execute() {
        return initSettings()
                .then(initDemoPart())
                .then();
    }

    private Mono<Void> initSettings() {
        return appSettingRepository.findById(DEFAULT_SETTINGS_ID)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Creating default application settings...");
                    AppSetting defaultSettings = AppSetting.builder()
                            .id(DEFAULT_SETTINGS_ID)
                            .lowStockThreshold(5)
                            .imageQuality(80)
                            .defaultPageSize(20)
                            .autoProcessAi(true)
                            .aiProvider("gemini")
                            .customApiKey("")
                            .updatedAt(Instant.now())
                            .build();
                    return appSettingRepository.save(defaultSettings);
                }))
                .then();
    }

    private Mono<Void> initDemoPart() {
        return partRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        log.info("Parts collection is empty. Creating demo component...");
                        Part demoPart = Part.builder()
                                .name("STM32F103C8T6 Microcontroller")
                                .type("IC")
                                .manufacturer("STMicroelectronics")
                                .partNumber("STM32F103C8T6")
                                .partCode("#1")
                                .location("Box A1")
                                .packageType("LQFP-48")
                                .mounting("SMD")
                                .quantity(10)
                                .description("Demo component: 32-bit ARM Cortex-M3 microcontroller, 72MHz, 64KB Flash, 20KB SRAM")
                                .photoIds(new ArrayList<>())
                                .metadata(Map.of("core", "ARM Cortex-M3", "voltage", "2.0V - 3.6V", "frequency", "72 MHz"))
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                        return partRepository.save(demoPart);
                    }
                    return Mono.empty();
                })
                .then();
    }
}
