package app.harven.partsmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartDictionaries {
    private String[] packages;
    private String[] manufacturers;
}
