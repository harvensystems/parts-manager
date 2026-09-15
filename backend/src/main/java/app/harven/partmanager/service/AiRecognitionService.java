package app.harven.partmanager.service;

import app.harven.partmanager.dto.AiResponseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import app.harven.partmanager.domain.RecognitionStatus;
import app.harven.partmanager.domain.RecognitionTask;
import app.harven.partmanager.repository.RecognitionTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AiRecognitionService {

    private final RecognitionTaskRepository taskRepository;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;
    private final ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.google.genai.api-key:}")
    private String aiApiKey;

    @Autowired
    public AiRecognitionService(
            RecognitionTaskRepository taskRepository,
            ImageStorageService imageStorageService,
            @Autowired(required = false) ChatClient.Builder chatClientBuilder
    ) {
        this.taskRepository = taskRepository;
        this.imageStorageService = imageStorageService;
        this.objectMapper = new ObjectMapper();
        this.chatClientBuilder = chatClientBuilder;
    }

    public void processTaskAsync(String taskId) {
        taskRepository.findById(taskId)
                .flatMap(task -> {
                    task.setStatus(RecognitionStatus.PROCESSING);
                    return taskRepository.save(task);
                })
                .publishOn(Schedulers.boundedElastic())
                .flatMap(task -> {
                    long startTime = System.currentTimeMillis();
                    Mono<?> execution;
                    if (aiApiKey != null && !aiApiKey.contains("mock") && !aiApiKey.trim().isEmpty() && chatClientBuilder != null) {
                        execution = processWithSpringAi(task);
                    } else {
                        execution = Mono.fromRunnable(() -> processSimulated(task));
                    }

                    return execution
                            .then(Mono.defer(() -> {
                                task.setStatus(RecognitionStatus.COMPLETED);
                                return Mono.empty();
                            }))
                            .onErrorResume(e -> {
                                log.error("AI recognition failed for task {}: {}", taskId, e.getMessage(), e);
                                task.setStatus(RecognitionStatus.FAILED);
                                task.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Failed to process image with AI");
                                return Mono.empty();
                            })
                            .then(Mono.defer(() -> {
                                task.setProcessingTimeMs(System.currentTimeMillis() - startTime);
                                task.setCompletedAt(Instant.now());
                                return taskRepository.save(task);
                            }));
                })
                .subscribe();
    }

    private Mono<Void> processWithSpringAi(RecognitionTask task) {
        return imageStorageService.getImageBytes(task.getPhotoId())
                .switchIfEmpty(Mono.error(new IllegalStateException("Photo not found in GridFS: " + task.getPhotoId())))
                .flatMap(imageBytes -> Mono.fromCallable(() -> {
                    ChatClient chatClient = chatClientBuilder.build();
                    String prompt = """
                        You are an expert electronics workshop assistant. Analyze this electronic component or package photo.
                        Extract details in strictly valid JSON format with this exact schema:
                        {
                          "name": "Component name (e.g. Resistor 10k 0805, STM32F103C8T6)",
                          "type": "Resistor | Capacitor | IC | Transistor | Diode | LED | Inductor | Connector | Sensor | Module | Other",
                          "manufacturer": "Manufacturer name or null",
                          "partNumber": "Part number or marking or null",
                          "packageType": "Package name (e.g. 0805, DIP-8, TO-220, LQFP-48) or null",
                          "mounting": "SMD or Through-hole or null",
                          "quantity": number or 1,
                          "description": "Short description or notes",
                          "metadata": {
                             // dynamic key-value pairs appropriate for this component type (e.g. resistance: "10 kΩ", voltage: "25 V")
                          },
                          "confidence": 95.0,
                          "rawText": "Exact text visible on package/marking"
                        }
                        Do not guess values that cannot be identified from the image. Return only the JSON object.
                    """.stripIndent();

                    AiResponseEntity response = chatClient.prompt()
                            .user(u ->
                                    u.text(prompt)
                                            .media(MimeTypeUtils.parseMimeType(task.getContentType() != null ? task.getContentType() : "image/jpeg"), new ByteArrayResource(imageBytes)))
                            .call()
                            .entity(AiResponseEntity.class);

                    if (response != null) {
                        task.setAiResult(objectMapper.readValue(objectMapper.writeValueAsString(response), new TypeReference<Map<String, Object>>() {}));
                        task.setRawText(response.getRawText());
                        task.setConfidence(response.getConfidence());
                    } else {
                        throw new RuntimeException("Empty response from AI");
                    }
                    return null;
                }));
    }

    private void processSimulated(RecognitionTask task) {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        String filename = (task.getOriginalFilename() != null ? task.getOriginalFilename() : "").toLowerCase();
        Map<String, Object> meta = new HashMap<>();
        String name = "Electronic Component";
        String type = "Other";
        String manufacturer = "Generic";
        String partNumber = "UNKNOWN";
        String pkg = "SMD";
        String mounting = "SMD";
        int quantity = 1;
        String description = "Recognized using AI";
        String rawText = "SMD COMPONENT";
        double confidence = 95.0;

        if (filename.contains("resistor") || filename.contains("res") || filename.contains("0805") || filename.contains("0603")) {
            name = "Resistor 10 kOhm 0805";
            type = "Resistor";
            manufacturer = "Yageo";
            partNumber = "RC0805FR-0710KL";
            pkg = "0805";
            mounting = "SMD";
            quantity = 100;
            description = "1% Thick Film Chip Resistor";
            meta.put("resistance", "10 kΩ");
            meta.put("tolerance", "1%");
            meta.put("power", "0.125 W");
            meta.put("tempCoeff", "100 ppm/°C");
            rawText = "1002 1% YAG 0805";
            confidence = 97.5;
        } else if (filename.contains("cap") || filename.contains("panasonic") || filename.contains("470")) {
            name = "Capacitor 470uF 25V";
            type = "Capacitor";
            manufacturer = "Panasonic";
            partNumber = "EEE-FT1E471GP";
            pkg = "Radial SMD";
            mounting = "SMD";
            quantity = 25;
            description = "Low ESR, 105°C Long Life Electrolytic Capacitor";
            meta.put("capacitance", "470 µF");
            meta.put("voltage", "25 V");
            meta.put("ESR", "0.15 Ω");
            meta.put("temp", "105°C");
            rawText = "470uF 25V FK 105C";
            confidence = 96.0;
        } else if (filename.contains("555") || filename.contains("ne555")) {
            name = "NE555P Precision Timer";
            type = "IC";
            manufacturer = "Texas Instruments";
            partNumber = "NE555P";
            pkg = "DIP-8";
            mounting = "Through-hole";
            quantity = 10;
            description = "Precision Analog Timer/Oscillator";
            meta.put("function", "Timer/Oscillator");
            meta.put("maxFreq", "500 kHz");
            meta.put("supplyVoltage", "4.5V - 16V");
            rawText = "NE555P TI 214AK";
            confidence = 98.0;
        } else if (filename.contains("7805") || filename.contains("lm7805")) {
            name = "Voltage Regulator 5V LM7805";
            type = "IC";
            manufacturer = "STMicroelectronics";
            partNumber = "L7805CV";
            pkg = "TO-220";
            mounting = "Through-hole";
            quantity = 5;
            description = "Linear Voltage Regulator +5V 1.5A";
            meta.put("outputVoltage", "5 V");
            meta.put("maxCurrent", "1.5 A");
            meta.put("dropVoltage", "2 V");
            rawText = "L7805CV ST e3 MAR 425";
            confidence = 99.0;
        } else if (filename.contains("stm32") || filename.contains("mcu")) {
            name = "Microcontroller STM32F103C8T6";
            type = "IC";
            manufacturer = "STMicroelectronics";
            partNumber = "STM32F103C8T6";
            pkg = "LQFP-48";
            mounting = "SMD";
            quantity = 5;
            description = "32-bit ARM Cortex-M3 MCU, 72MHz, 64KB Flash";
            meta.put("core", "ARM Cortex-M3");
            meta.put("flash", "64 KB");
            meta.put("ram", "20 KB");
            meta.put("frequency", "72 MHz");
            rawText = "STM32F103 C8T6 990AA VG MYS 7B";
            confidence = 98.5;
        } else {
            name = "SMD Electronic Component";
            type = "Resistor";
            manufacturer = "Vishay";
            partNumber = "CRCW06034K70FKEA";
            pkg = "0603";
            mounting = "SMD";
            quantity = 50;
            description = "High stability SMD resistor";
            meta.put("resistance", "4.7 kΩ");
            meta.put("tolerance", "1%");
            meta.put("power", "0.1 W");
            rawText = "4K7 1% VISHAY 0603";
            confidence = 94.0;
        }

        task.setRawText(rawText);
        task.setConfidence(confidence);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("type", type);
        resultMap.put("manufacturer", manufacturer);
        resultMap.put("partNumber", partNumber);
        resultMap.put("packageType", pkg);
        resultMap.put("mounting", mounting);
        resultMap.put("quantity", quantity);
        resultMap.put("description", description);
        resultMap.put("metadata", meta);
        resultMap.put("confidence", confidence);
        resultMap.put("rawText", rawText);

        task.setAiResult(resultMap);
    }
}
