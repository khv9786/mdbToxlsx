package com.mdbcounter.repository.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DbDao {

    /**
     * DB에 존재하는 테이블 목록 얻기
     */
    public Set<String> getDbTableName(Connection conn) throws SQLException {
        Set<String> dbTableNames = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                dbTableNames.add(tableName);
            }
        }
        return dbTableNames;
    }
}