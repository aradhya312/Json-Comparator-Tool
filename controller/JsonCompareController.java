package jsoncomparison.controller;

import jsoncomparison.domain.ApiConfigs;
import jsoncomparison.domain.DatabaseConfigs;
import jsoncomparison.domain.RequestData;
import jsoncomparison.domain.RespondData;
import jsoncomparison.exception.InvalidUserQueryException;
import jsoncomparison.service.ApiConnectionService;
import jsoncomparison.service.HandlerService;
import jsoncomparison.util.JsonCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;

import static jsoncomparison.constants.JsonCompareConstants.*;

@Slf4j
@RestController
public class JsonCompareController {
    @Autowired
    HandlerService handlerService;

    @Autowired
    private ApiConnectionService apiConnectionService;

    @PostMapping(JSON_COMPARE_BY_DATABASE_END_POINT)
    /**
     *   Handles an API call to compare data based on the provided database configurations.
     */
    public ResponseEntity<Resource> compareDataByDatabase(@RequestBody DatabaseConfigs databaseConfigs) {
        try {
            byte[] excelData = handlerService.processHandler(databaseConfigs);
            ByteArrayResource resource = new ByteArrayResource(excelData);
            String fileName = JsonCompareUtil.generateUniqueFileName("jsonDifferences_");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "force-download"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelData.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (SQLException e) {
            log.error("SQL Exception occured while processing", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (IOException e) {
            log.error("IO Exception occured while processing", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } catch (InvalidUserQueryException e) {
            log.error("Invalid Query provided by the user");
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("/process/{id}")
    public RespondData processRequest(@RequestBody RequestData requestData) {
        RespondData responseData = new RespondData();
        try {
            responseData.setStatus("success");
            responseData.setMessage("Processed data for field1: " + requestData.getField1());
            responseData.setMessage("Processed data for field2: " + requestData.getField2());

        } catch (Exception e) {
            responseData.setMessage("error");
        }
        return responseData;
    }
    }
