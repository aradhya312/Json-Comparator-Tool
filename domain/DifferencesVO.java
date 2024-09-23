package jsoncomparison.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifferencesVO {
    private String uniqueNumber;
    private String jsonTag;
    private String description;
    private String database1Value;
    private String database2Value;
}
