package jsoncomparison.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jsoncomparison.domain.DifferencesVO;
import jsoncomparison.util.JsonCompareUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static jsoncomparison.constants.JsonCompareConstants.*;


@Service
@Slf4j
public class JsonComparatorService {

    /*
    Method for comparing the two json data
     */
    public List<DifferencesVO> compareJson(String json1, String json2, String uniqueId) throws IOException {
        List<DifferencesVO> differences = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode1 = objectMapper.readValue(json1, JsonNode.class);
        JsonNode jsonNode2 = objectMapper.readValue(json2, JsonNode.class);
        return compareJsonNodes(uniqueId, jsonNode1, jsonNode2, differences);
    }

    private List<DifferencesVO> compareJsonNodes(String uniqueId, JsonNode node1, JsonNode node2, List<DifferencesVO> differences) {
        compareFields(uniqueId, node1, node2, differences, "");
        return differences;
    }

    private static void compareFields(String uniqueId, JsonNode node1, JsonNode node2, List<DifferencesVO> differences, String fieldPath) {
        Iterator<String> fieldNames1 = node1.fieldNames();
        Iterator<String> fieldNames2 = node2.fieldNames();

        while (fieldNames1.hasNext()) {
            String fieldName = fieldNames1.next();
            String currentFieldPath = fieldPath.isEmpty() ? fieldName : fieldPath + "." + fieldName;

            if (!node2.has(fieldName)) {
                differences.add(JsonCompareUtil.createDifferences(uniqueId, currentFieldPath, FIELD_MISSING_IN_SECOND_FILE, node1.get(fieldName).toString(), " "));
                continue;
            }

            JsonNode value1 = node1.get(fieldName);
            JsonNode value2 = node2.get(fieldName);

            if (!value1.equals(value2)) {
                if (value1.isObject() && value2.isObject()) {
                    compareFields(uniqueId, value1, value2, differences, currentFieldPath);
                } else if (value1.isArray() && value2.isArray()) {
                    compareArraysAfterSorting(uniqueId, value1, value2, differences, currentFieldPath);
                } else {
                    differences.add(JsonCompareUtil.createDifferences(uniqueId, currentFieldPath, DIFFERENT_VALUES, value1.toString(), value2.toString()));
                }
            }
        }
        while (fieldNames2.hasNext()) {
            String fieldName = fieldNames2.next();
            String currentFieldPath = fieldPath.isEmpty() ? fieldName : fieldPath + "." + fieldName;

            if (!node1.has(fieldName)) {
                differences.add(JsonCompareUtil.createDifferences(uniqueId, currentFieldPath, FIELD_MISSING_IN_FIRST_FILE, " ", node2.get(fieldName).toString()));
            }
        }
    }
    private static void compareArraysAfterSorting(String uniqueId, JsonNode array1, JsonNode array2, List<DifferencesVO> differences, String arrayPath) {
        List<JsonNode> list1 = JsonCompareUtil.convertJsonArrayToList(array1);
        List<JsonNode> list2 = JsonCompareUtil.convertJsonArrayToList(array2);

        if (list1.size() != (list2.size())) {
            differences.add(JsonCompareUtil.createDifferences(uniqueId, arrayPath, ARRAY_SIZE_DIFFERENCE, String.valueOf(list1.size()), String.valueOf(list2.size())));
            return;
        }

       // list1.sort(Comparator.comparing(JsonNode::toString));
       // list2.sort(Comparator.comparing(JsonNode::toString));


        for (int i = 0; i < list1.size(); i++) {
            JsonNode element1 = list1.get(i);
            JsonNode element2 = list2.get(i);
            String currentArrayPath = arrayPath + "[" + i + "]";

            if (!element1.equals(element2)) {
                if (!element1.isObject() && !element1.isArray()) {
                    differences.add(JsonCompareUtil.createDifferences(uniqueId, currentArrayPath, " ", element1.toString(), element2.toString()));
                } else {
                    compareFields(uniqueId, element1, element2, differences, currentArrayPath);
                }
            }
        }
    }
    /*
    Method to compare json data if it is found null in any file
     */
    public List<DifferencesVO> compareNullJson(String json1, String json2, String uniqueId) {
        List<DifferencesVO> differences = new ArrayList<>();
        if (json1 == null && json2 != null) {
            differences.add(JsonCompareUtil.createDifferences(uniqueId, NULL_DATA_FOUND, DIFFERENT_VALUES, NULL, NOT_NULL));
        }
        else if (json1 != null && json2 == null) {
            differences.add(JsonCompareUtil.createDifferences(uniqueId, NULL_DATA_FOUND, DIFFERENT_VALUES, NOT_NULL, NULL));
        }
        else{
                log.warn("Json data is null in both the databases with the id : "+uniqueId);
            }
        return differences;
    }
}