package app.harven.partsmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryResponseDto {
    private List<String> manufacturers;
    private List<String> packages;
    private List<String> parameters;
    private List<String> components;
    private Boolean enabledAI;
}
