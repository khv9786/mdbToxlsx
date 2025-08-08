package com.mdbcounter.service;


import com.mdbcounter.model.TableCount;

import java.io.File;
import java.sql.*;
import java.util.*;

public class MdbCounterService {
    public static final String MDB_EXT = ".mdb";

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
                result.add(new TableCount.Builder().tableName(table).count(totalCount).build());
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
//
//    // 비어있지 않은 데이터 모두 더하기. -> 기존 쿼리 하나하나 보내서 너무 답답ㄷ@!~##@!@#!
//    private int sumCntCol1(Connection conn, String table) throws SQLException {
//        int sum = 0;
//        DatabaseMetaData meta = conn.getMetaData();
//        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
//            while (cols.next()) {
//                String colName = cols.getString("COLUMN_NAME");
//                String sql = "SELECT COUNT([" + colName + "]) FROM [" + table + "] WHERE [" + colName + "] IS NOT NULL";
//                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        sum += rs.getInt(1);
//                    }
//                }
//            }
//        }
//        return sum;
//    }

    // 단일쿼리로 수정. 속도는 44sec -> 32sec
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
     * mdb 파일내용 정리, 테이블 명, 몇개 있는지
     *
     * @param mdbFiles
     * @return
     */
    public Map<String, Integer> aggregationMdbFile(List<File> mdbFiles) {
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