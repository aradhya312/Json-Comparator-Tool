package jsoncomparison.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfigs {
    private String databaseUrl_1 ;
    private String username_1;
    private String password_1;

    private String databaseUrl_2 ;
    private String username_2;
    private String password_2;

    private String schema_1;
    private String schema_2;

    private String tableName_1;
    private String tableName_2;

    private String uniqueKey_1;
    private String jsonDataColumn_1;

    private String uniqueKey_2;
    private String jsonDataColumn_2;

    private String userQuery_1;
    private String userQuery_2;

    private List<String> idsToCompare;

    public String getSchema1(){
        if(!schema_1.isEmpty())
            return schema_1+".";
        return "";
    }
    public String getSchema2(){
        if(!schema_2.isEmpty())
            return schema_1+".";
        return "";
    }

}
