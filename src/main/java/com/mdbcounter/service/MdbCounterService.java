package com.mdbcounter.service;

import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.model.TableCount;

import java.io.File;
import java.sql.*;
import java.util.*;

public class MdbCounterService {
    public static final String MDB_EXT = ".mdb";

    /**
     * 각 테이블의 총 데이터 개수를 반환
     * @param mdb MDB 파일
     * @return 테이블별 총 데이터 개수 List<TableCount>
     */
    public List<TableCount> countTableColumnData(File mdb) {
        List<TableCount> result = new ArrayList<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String table : getTableNames(conn)) {
                int totalCount = sumOfColumnCounts(conn, table);
                result.add(new TableCount.Builder()
                        .tableName(table)
                        .count(totalCount)
                        .build());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + mdb.getName() + " 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private List<String> getTableNames(Connection conn) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    // 비어있지 않은 데이터 모두 더하기.
    private int sumOfColumnCounts(Connection conn, String table) throws SQLException {
        int sum = 0;
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String sql = "SELECT COUNT([" + colName + "]) FROM [" + table + "] WHERE [" + colName + "] IS NOT NULL";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sum += rs.getInt(1);
                    }
                }
            }
        }
        return sum;
    }
}