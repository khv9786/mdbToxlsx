package com.mdbcounter.service;

import com.mdbcounter.model.ColumnCount;
import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * MDB 파일의 테이블별 컬럼 데이터 개수를 카운트하는 서비스 클래스
 * UCanAccess 라이브러리를 사용하여 Access 데이터베이스 파일을 읽고,
 * 각 테이블의 컬럼별로 유효한 데이터 개수를 계산합니다.
 */
public class MdbCounterService {
    /** MDB 파일 확장자 */
    public static final String MDB_EXT = ".mdb";

    /**
     * 여러 MDB 파일에서 파일명별(시트명)로 테이블별 컬럼 데이터 개수를 카운트합니다.
     * @param mdbFiles 처리할 MDB 파일 목록
     * @return 파일명을 키로 하고, 테이블별 컬럼카운트 Map을 값으로 하는 Map
     */
    public Map<String, Map<String, List<ColumnCount>>> countFileTableColumns(List<File> mdbFiles) {
        System.setProperty("net.ucanaccess.quiet", "true");
        long startTime  = System.currentTimeMillis();
        Map<String, Map<String, List<ColumnCount>>> fileTableMap = new LinkedHashMap<>();
        for (File mdb : mdbFiles) {
            fileTableMap.put(getFileNameWithoutExt(mdb), processOneFile(mdb));
        }
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("로딩 시간:"+timeElapsed/1000+"sec");
        return fileTableMap;
    }

    /**
     * 하나의 MDB 파일을 처리하여 테이블별 컬럼 카운트 Map을 반환
     */
    private Map<String, List<ColumnCount>> processOneFile(File mdb) {
        Map<String, List<ColumnCount>> tableMap = new LinkedHashMap<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String table : getTableNames(conn)) {
                tableMap.put(table, processOneTable(conn, table));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + mdb.getName() + " 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[INFO] 처리 완료 파일명:" + mdb.getAbsolutePath());
        return tableMap;
    }

    /**
     * DB에서 테이블명 목록을 가져옴
     */
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
     * 하나의 테이블에서 컬럼별 데이터 개수 리스트 반환
     */
    private List<ColumnCount> processOneTable(Connection conn, String table) throws SQLException {
        List<ColumnCount> columns = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                String colType = cols.getString("TYPE_NAME");
                int cnt = countValid(conn, table, colName, colType);
                columns.add(new ColumnCount.Builder()
                    .tableName(table)
                    .columnName(colName)
                    .count(cnt)
                    .build());
            }
        }
        return columns;
    }

    /**
     * 파일명에서 확장자 제거
     */
    private String getFileNameWithoutExt(File file) {
        String name = file.getName();
        return name.toLowerCase().endsWith(MDB_EXT) ? name.substring(0, name.length() - MDB_EXT.length()) : name;
    }

    /**
     * 특정 테이블의 특정 컬럼에서 유효한 데이터 개수를 카운트합니다.
     * 데이터 타입에 따라 다른 조건을 적용하여 정확한 카운트를 수행합니다.
     * 
     * @param conn 데이터베이스 연결 객체
     * @param table 테이블명
     * @param col 컬럼명
     * @param type 컬럼의 데이터 타입
     * @return 유효한 데이터 개수
     * @throws SQLException SQL 실행 중 오류 발생 시
     */
    private int countValid(Connection conn, String table, String col, String type) throws SQLException {
        String sql;
        type = type.toUpperCase();
        // 데이터 타입에 따라 다른 쿼리 조건 적용
        if (type.contains("CHAR") || type.contains("TEXT")) {
            sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [" + col + "] IS NOT NULL AND [" + col + "] <> '' AND [" + col + "] <> '0'";
        } else if (type.contains("INT") || type.contains("NUMERIC") || type.contains("DECIMAL") || type.contains("DOUBLE") || type.contains("FLOAT")) {
            sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [" + col + "] IS NOT NULL AND [" + col + "] <> 0";
        } else {
            sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [" + col + "] IS NOT NULL";
        }
        try (PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
} 