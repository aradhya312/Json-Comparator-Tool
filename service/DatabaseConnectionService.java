package jsoncomparison.service;

import jsoncomparison.domain.DatabaseConfigs;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatabaseConnectionService {

    /**
     *     Method for establish database connections
     */
    public Pair<Connection, Connection> establishDatabaseConnections(DatabaseConfigs databaseConfigs) throws SQLException {
        try {
            Connection connection_1 = DriverManager.getConnection(databaseConfigs.getDatabaseUrl_1(), databaseConfigs.getUsername_1(), databaseConfigs.getPassword_1());
            Connection connection_2 = DriverManager.getConnection(databaseConfigs.getDatabaseUrl_2(), databaseConfigs.getUsername_2(), databaseConfigs.getPassword_2());
            log.info("Connected to databases successfully");
            return Pair.of(connection_1, connection_2);
        } catch (SQLException e) {
            log.error("SQL Exception occured ", e.getMessage(), e);
            throw new SQLException(e.getMessage(), e);
        }
    }

    /**
     *     Method for creating statement and execute query
     */
    public Pair<Map<String, String>, Map<String, String>> executeDatabaseQuery(DatabaseConfigs databaseConfigs, Connection connection_1, Connection connection_2) throws SQLException, IOException {
        try {
            String queryTemplate_1 = "SELECT * FROM " + databaseConfigs.getSchema1() + databaseConfigs.getTableName_1() + " WHERE " + databaseConfigs.getUniqueKey_1() + " IN (%s)";
            String queryTemplate_2 = "SELECT * FROM " + databaseConfigs.getSchema2() + databaseConfigs.getTableName_2() + " WHERE " + databaseConfigs.getUniqueKey_2() + " IN (%s)";

            String idString = databaseConfigs.getIdsToCompare().stream().map(id -> "?").collect(Collectors.joining(", "));

            try (PreparedStatement statement_1 = connection_1.prepareStatement(String.format(queryTemplate_1, idString));
                 PreparedStatement statement_2 = connection_2.prepareStatement(String.format(queryTemplate_2, idString))) {

                for (int j = 0; j < databaseConfigs.getIdsToCompare().size(); j++) {
                    statement_1.setString(j + 1, databaseConfigs.getIdsToCompare().get(j));
                    statement_2.setString(j + 1, databaseConfigs.getIdsToCompare().get(j));
                }
                Map<String, String> resultSetMap_1 = new HashMap<>();
                Map<String, String> resultSetMap_2 = new HashMap<>();

                try (ResultSet resultSet_1 = statement_1.executeQuery()) {
                    while (resultSet_1.next()) {
                        String id_1 = resultSet_1.getString(databaseConfigs.getUniqueKey_1());
                        String json_1 = resultSet_1.getString(databaseConfigs.getJsonDataColumn_1());
                        resultSetMap_1.put(id_1, json_1);
                    }
                }
                try (ResultSet resultSet_2 = statement_2.executeQuery()) {
                    while (resultSet_2.next()) {
                        String id_2 = resultSet_2.getString(databaseConfigs.getUniqueKey_2());
                        String json_2 = resultSet_2.getString(databaseConfigs.getJsonDataColumn_2());
                        resultSetMap_2.put(id_2, json_2);
                    }
                }
                return Pair.of(resultSetMap_1, resultSetMap_2);
            }
        } catch (SQLException e) {
            log.error("Sql Exception occured in databaseQuery", e.getMessage(), e);
            throw new SQLException(e.getMessage(), e);
        }
    }

    public Pair<Map<String, String>, Map<String, String>> executeUserQueries(DatabaseConfigs databaseConfigs, Connection connection_1, Connection connection_2) throws SQLException, IOException {
        try {
            String userQuery_1 = databaseConfigs.getUserQuery_1();
            String userQuery_2 = databaseConfigs.getUserQuery_2();

            Map<String, String> resultSetMap_1 = new HashMap<>();
            Map<String, String> resultSetMap_2 = new HashMap<>();

            if (isSingleQuery(userQuery_1) && isValidSelectStatement(userQuery_1) && isSingleQuery(userQuery_2) && isValidSelectStatement(userQuery_2)) {
                try (PreparedStatement statement_1 = connection_1.prepareStatement(userQuery_1);
                     ResultSet resultSet_1 = statement_1.executeQuery()) {
                    while (resultSet_1.next()) {
                        String id_1 = resultSet_1.getString(1);
                        String json_1 = resultSet_1.getString(2);
                        resultSetMap_1.put(id_1, json_1);
                    }
                }
                try (PreparedStatement statement_2 = connection_2.prepareStatement(userQuery_2);
                     ResultSet resultSet_2 = statement_2.executeQuery()) {
                    while (resultSet_2.next()) {
                        String id_2 = resultSet_2.getString(1);
                        String json_2 = resultSet_2.getString(2);
                        resultSetMap_2.put(id_2, json_2);
                    }
                }
            } else {
                log.error("Invalid user queries. Both queries must be single SELECT statements");
                return null;
            }
            return Pair.of(resultSetMap_1, resultSetMap_2);

        } catch (SQLException e) {
            log.error("Sql Exception occurred in executing user queries", e);
            throw new SQLException(e.getMessage(), e);
        }
    }

    private boolean isSingleQuery(String userQuery) {
        return !userQuery.contains(";");
    }

    private boolean isValidSelectStatement(String userQuery) {
        return userQuery.trim().toUpperCase().startsWith("SELECT");
    }
}


