package app.harven.partsmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartDictionaries {
    private List<String> packages;
    private List<String> manufacturers;
}
