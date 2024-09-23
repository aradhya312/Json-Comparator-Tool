package jsoncomparison.util;

import com.fasterxml.jackson.databind.JsonNode;
import jsoncomparison.domain.DifferencesVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static jsoncomparison.constants.JsonCompareConstants.*;

public class JsonCompareUtil {

    public static DifferencesVO createDifferences(String uniqueNumber, String jsonTag, String description, String database1Value, String database2Value) {
        DifferencesVO differences = new DifferencesVO();
        differences.setUniqueNumber(uniqueNumber);
        differences.setJsonTag(jsonTag);
        differences.setDescription(description);
        differences.setDatabase1Value(database1Value);
        differences.setDatabase2Value(database2Value);
        return differences;
    }

    public static List<JsonNode> convertJsonArrayToList(JsonNode array) {
        List<JsonNode> list = new ArrayList<>();
        for (JsonNode element : array) {
            list.add(element);
        }
        return list;
    }
    public static String generateUniqueFileName(String baseName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP);
        String timestamp = LocalDateTime.now().format(formatter);
        return baseName + timestamp + OUTPUT_FILE;
    }
}

