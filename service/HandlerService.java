package jsoncomparison.service;

import jsoncomparison.domain.DatabaseConfigs;
import jsoncomparison.domain.DifferencesVO;
import jsoncomparison.exception.InvalidUserQueryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static jsoncomparison.constants.JsonCompareConstants.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class HandlerService {

    @Autowired
    DatabaseConnectionService databaseconnectionservice;
    @Autowired
    WriteDifferencesToExcelService writeDifferenceToExcel;
    @Autowired
    JsonComparatorService jsonComparatorService;

    /**
     *     Method to handle the methods
      */
    public byte[] processHandler(DatabaseConfigs databaseConfigs) throws SQLException, IOException, InvalidUserQueryException {
        Pair<Connection, Connection> connections = null;
        try {
            connections = databaseconnectionservice.establishDatabaseConnections(databaseConfigs);

            List<DifferencesVO> differencesVOS = new ArrayList<>();
            if (!isEmpty(databaseConfigs.getUserQuery_1()) && !isEmpty(databaseConfigs.getUserQuery_2())) {
                Pair<Map<String, String>, Map<String, String>> resultMap = databaseconnectionservice.executeUserQueries(databaseConfigs, connections.getLeft(), connections.getRight());
                differencesVOS.addAll(processResultMap(resultMap));
            } else if (isEmpty(databaseConfigs.getUserQuery_1()) && isEmpty(databaseConfigs.getUserQuery_2())) {
                List<String> idsToCompare = databaseConfigs.getIdsToCompare();

                for (int i = 0; i < idsToCompare.size(); i += BATCH_SIZE) {
                    databaseConfigs.setIdsToCompare(idsToCompare.subList(i, Math.min(i + BATCH_SIZE, idsToCompare.size())));
                    Pair<Map<String, String>, Map<String, String>> resultMap = databaseconnectionservice.executeDatabaseQuery(databaseConfigs, connections.getLeft(), connections.getRight());
                    differencesVOS.addAll(processResultMap(resultMap));
                }
            } else {
                log.error("Both queries must be non-empty for comparison. Discarding results.");
                throw new InvalidUserQueryException("Invalid Query");
            }
            return writeDifferenceToExcel.writeDifferencesToExcel(differencesVOS);

        } catch (IOException e) {
            log.error("IOException occured while processing the data", e);
            throw new IOException(e.getMessage(), e);

        } catch (SQLException e) {
            log.error("SQL Exception occured in Handler", e);
            throw new SQLException(e.getMessage(), e);
        } finally {
            try {
                connections.getLeft().close();
                connections.getRight().close();
            } catch (SQLException e) {
                log.error("Error in closing the database connections", e.getMessage(), e);
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    private List<DifferencesVO> processResultMap(Pair<Map<String, String>, Map<String, String>> resultMap) throws IOException {
        List<DifferencesVO> differencesVOS = new ArrayList<>();
        Set<String> allKeys = new HashSet<>(resultMap.getLeft().keySet());
        allKeys.addAll(resultMap.getRight().keySet());

        for (String id : allKeys) {
            String json_1 = resultMap.getLeft().get(id);
            String json_2 = resultMap.getRight().get(id);

            if (json_1 == null || json_2 == null) {
                differencesVOS.addAll(jsonComparatorService.compareNullJson(json_1, json_2, id));
            } else {
                differencesVOS.addAll(jsonComparatorService.compareJson(json_1, json_2, id));
            }
        }
        return differencesVOS;
    }
    private boolean isEmpty(String userQuery) {
        return userQuery == null || userQuery.trim().isEmpty();
    }
}

