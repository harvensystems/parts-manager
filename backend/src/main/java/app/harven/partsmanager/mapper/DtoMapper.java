package app.harven.partsmanager.mapper;

import app.harven.partsmanager.domain.AppSetting;
import app.harven.partsmanager.domain.Part;
import app.harven.partsmanager.domain.RecognitionTask;
import app.harven.partsmanager.dto.AppSettingDto;
import app.harven.partsmanager.dto.PartResponseDto;
import app.harven.partsmanager.dto.RecognitionTaskResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component
public class DtoMapper {

    public AppSettingDto toAppSettingDto(AppSetting setting) {
        if (setting == null) {
            return null;
        }
        return AppSettingDto.builder()
                .lowStockThreshold(setting.getLowStockThreshold())
                .imageQuality(setting.getImageQuality())
                .defaultPageSize(setting.getDefaultPageSize())
                .autoProcessAi(setting.getAutoProcessAi())
                .aiProvider(setting.getAiProvider())
                .customApiKey(setting.getCustomApiKey())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    public PartResponseDto toPartResponseDto(Part part) {
        if (part == null) {
            return null;
        }
        return PartResponseDto.builder()
                .id(part.getId())
                .name(part.getName())
                .type(part.getType())
                .manufacturer(part.getManufacturer())
                .partNumber(part.getPartNumber())
                .packageType(part.getPackageType())
                .mounting(part.getMounting())
                .quantity(part.getQuantity())
                .description(part.getDescription())
                .photoIds(part.getPhotoIds() != null ? new ArrayList<>(part.getPhotoIds()) : new ArrayList<>())
                .metadata(part.getMetadata() != null ? new HashMap<>(part.getMetadata()) : new HashMap<>())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }

    public RecognitionTaskResponseDto toRecognitionTaskResponseDto(RecognitionTask task) {
        if (task == null) {
            return null;
        }
        return RecognitionTaskResponseDto.builder()
                .id(task.getId())
                .photoId(task.getPhotoId())
                .originalFilename(task.getOriginalFilename())
                .contentType(task.getContentType())
                .status(task.getStatus())
                .aiResult(task.getAiResult() != null ? new HashMap<>(task.getAiResult()) : new HashMap<>())
                .rawText(task.getRawText())
                .confidence(task.getConfidence())
                .errorMessage(task.getErrorMessage())
                .processingTimeMs(task.getProcessingTimeMs())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
