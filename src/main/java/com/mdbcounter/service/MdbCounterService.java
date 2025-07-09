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
            System.out.println("[INFO] 처리 시작: " + mdb.getAbsolutePath());
            String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
            Map<String, List<ColumnCount>> tableMap = new LinkedHashMap<>();
            try (Connection conn = DriverManager.getConnection(url)) {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String table = tables.getString("TABLE_NAME");
//                        System.out.println("  [INFO] 테이블: " + table);
                        List<ColumnCount> columns = new ArrayList<>();
                        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
                            while (cols.next()) {
                                String colName = cols.getString("COLUMN_NAME");
                                String colType = cols.getString("TYPE_NAME");
                                int cnt = countValid(conn, table, colName, colType);
                                columns.add(new ColumnCount(table, colName, cnt));
//                                System.out.println("    [INFO] 컬럼: " + colName + " → " + cnt + "개");
                            }
                        }
                        tableMap.put(table, columns);
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] " + mdb.getName() + " 처리 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("[INFO] 처리 완료: " + mdb.getAbsolutePath());
            // 파일명(확장자 제외) 기준으로 Map에 저장
            String fileName = mdb.getName();
            if (fileName.toLowerCase().endsWith(MDB_EXT)) {
                fileName = fileName.substring(0, fileName.length() - MDB_EXT.length());
            }
            fileTableMap.put(fileName, tableMap);
        }
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.println("로딩 시간:"+timeElapsed/1000+"sec");
        return fileTableMap;
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
        // 쿼리 실행 및 결과 반환
        try (PreparedStatement ps = conn.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
} 