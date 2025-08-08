package com.mdbcounter.service;


import com.mdbcounter.model.TableCount;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MdbCounterService {
    public static final String MDB_EXT = ".mdb";
    public static final String COL_NAME = "r_stream";

    /**
     * 각 테이블의 총 데이터 개수를 반환
     *
     * @param mdb MDB 파일
     * @return 테이블별 총 데이터 개수 List<TableCount>
     */
    public List<TableCount> countTableColumnData(File mdb) {
        List<TableCount> result = new ArrayList<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String table : getTableNames(conn)) {
                int totalCount = sumCntCol(conn, table);
                result.add(new TableCount.Builder()
                        .tableName(table)
                        .count(totalCount)
                        .build());
            }
        } catch (Exception e) {
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

    /**
     * 특정 테이블에 있는 데이터 싹~~~ 끌어오기
     * @param conn
     * @param table
     * @return
     * @throws SQLException
     */
    private int sumCntCol(Connection conn, String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                columns.add(cols.getString("COLUMN_NAME"));
            }
        }

        if (columns.isEmpty()) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(" + ");
            sql.append("COUNT([").append(columns.get(i)).append("])");
        }
        sql.append(" FROM [").append(table).append("]");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * 특정 컬럼명으 개수 정리.
     * @param conn
     * @param table
     * @param colName
     * @return
     * @throws SQLException
     */
    private int sumSpecificCntCol(Connection conn, String table, String colName) throws SQLException {
        //특정 컬럼 확인
        DatabaseMetaData meta = conn.getMetaData();
        boolean hasColumn = false;
        try (ResultSet cols = meta.getColumns(null, null, table, null)) {
            while (cols.next()) {
                if (colName.equalsIgnoreCase(cols.getString("COLUMN_NAME"))) {
                    hasColumn = true;
                    break;
                }
            }
        }
        if (!hasColumn) {
            return 0; // 컬럼 없으면 바로 반환
        }

        String sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [" + COL_NAME + "] IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * mdb에서 테이블에 null 아닌 값 모두 정리.
     *
     * @param mdbFiles
     * @return
     */
    public Map<String, Integer> calMdbTableCnt(List<File> mdbFiles) {
        Map<String, Integer> tableTotalMap = new LinkedHashMap<>();
        for (File mdb : mdbFiles) {
            List<TableCount> oneFileCounts = countTableColumnData(mdb);
            for (TableCount tableCount : oneFileCounts) {
                String tableName = tableCount.getTableName();
                int currentCount = tableTotalMap.getOrDefault(tableName, 0);
                tableTotalMap.put(tableName, currentCount + tableCount.getCount());
            }
        }
        return tableTotalMap;
    }

    /**
     * Map에 담은 데이터 정리.
     *
     * @param tableTotalMap
     * @return
     */
    public List<TableCount> convertMapToList(Map<String, Integer> tableTotalMap) {
        List<TableCount> allTableCounts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tableTotalMap.entrySet()) {
            allTableCounts.add(new TableCount.Builder().tableName(entry.getKey()).count(entry.getValue()).build());
        }
        return allTableCounts;
    }
}