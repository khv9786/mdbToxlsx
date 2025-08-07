package com.mdbcounter.service;

import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * MDB와 PostgreSQL DB를 비교하는 서비스
 */
public class DbComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(DbComparisonService.class);
    public static final String COLNAME = "r_stream";
    private static final Map<String, String> UPPER_TABLE;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("rim027", "\"RIM027\"");
        map.put("rim047", "\"RIM047\"");
        map.put("rim0100", "\"RIM0100\"");
        map.put("RIM027", "\"RIM027\"");
        map.put("RIM047", "\"RIM047\"");
        map.put("RIM0100", "\"RIM0100\"");
        UPPER_TABLE = Collections.unmodifiableMap(map);
    }
    private static final Map<String, String> RESERVEDWORD;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("cross", "\"cross\"");
        map.put("CROSS", "\"cross\"");
        RESERVEDWORD = Collections.unmodifiableMap(map);
    }

    /**
     * MDB와 DB를 비교하여 결과를 반환
     *
     * @param mdbTableInfos MDB 테이블 정보 리스트
     * @return 비교 결과
     */
    public ComparisonResult compareMdbWithDbOptimized(List<MdbTableInfo> mdbTableInfos) {
        logger.info("==== 비교 로직 시작 =====");
        List<ComparisonResult.MissingTableInfo> missingTables = new ArrayList<>();
        List<ComparisonResult.MissingKeyInfo> missingKeys = new ArrayList<>();
        List<ComparisonResult.CompareCntInfo> compareCnt = new ArrayList<>();

        try (Connection dbConn = DatabaseConfig.getConnection()) {
            //  DB에 존재하는 테이블 목록 정리.
            Set<String> dbTables = getDbTables(dbConn);
            logger.info("DB에 존재하는 테이블 개수: {}", dbTables.size());
            logger.info("MDB내 테이블 개수: {}", mdbTableInfos.size());
            for (MdbTableInfo mdbTable : mdbTableInfos) {

                // MDB 테이블 명 정리.
                String mdbFileName = mdbTable.getMdbFileName(); // MDB 파일이름
                String tableName = mdbTable.getTableName(); // MDB 테이블 이름
                String NormalizedTableName = mdbTable.getNormalizedMdbColName(); // 정규화한 테이블 이름

                // 2. MDB 테이블이 DB에 있는지 체크
                if (!dbTables.contains(NormalizedTableName)) {
                    // 데이터 넣을때는 MDB 형식으로 넣어줌.
                    missingTables.add(new ComparisonResult.MissingTableInfo(mdbFileName, NormalizedTableName));
                    continue;
                }
                String fillterTableName = convertTableCorrectName(NormalizedTableName);

                // 3. 있다면 r_stream 값과, 개수가 몇개인지 map으로 반환.
                Map<String, Integer> mdbRStreams = mdbTable.getRStreamValues();

                if (!mdbRStreams.isEmpty()) {

                    Iterator<String> keys = mdbRStreams.keySet().iterator();
                    while (keys.hasNext()) {
                        String rStream = keys.next();
                        int mdbCount = mdbRStreams.get(rStream);
                        int dbCount = getRStreamCount(dbConn, fillterTableName, rStream);

                        // 모든 r_stream 값에 대해 개수 비교 정보 저장
                        compareCnt.add(new ComparisonResult.CompareCntInfo(
                            mdbFileName, tableName, rStream, mdbCount, dbCount));

                        if (dbCount == 0) {
//                            logger.info("MDB {} 에는 있지만 DB에 없는 R_stream 값 발견: {} (테이블: {} )", mdbFileName, rStream, fillterTableName);
                            missingKeys.add(new ComparisonResult.MissingKeyInfo(mdbTable.getMdbFileName(), tableName, rStream));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("DB 비교 중 오류: {}", e.getMessage(), e);
        }

//        logger.info("비교 완료 - 누락된 테이블: {}, 누락된 키: {}", missingTables.size(), missingKeys.size());
        
        return new ComparisonResult.Builder()
                .missingTables(missingTables)
                .missingKeys(missingKeys)
                .compareCnt(compareCnt)
                .build();
    }


    /**
     * DB에 존재하는 테이블 목록 얻기
     */
    private Set<String> getDbTables(Connection conn) throws SQLException {
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

    /**
     * 특정 r_stream가 DB에 몇개가 있는지 조회
     */
    private int getRStreamCount(Connection conn, String tableName, String rStream) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + COLNAME + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rStream);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * MDB 파일에서 테이블 정보와 R_stream 값을 읽어서 반환
     *
     * @param mdb MDB 파일
     * @return MDB 테이블 정보 리스트
     */
    public List<MdbTableInfo> getMdbTableInfo(File mdb) {
        List<MdbTableInfo> result = new ArrayList<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        String mdbFileName = mdb.getName().replaceAll("\\.mdb$", ""); // .mdb 확장자 제거

        try (Connection conn = DriverManager.getConnection(url)) {
            Set<String> tableNames = getMDbTableNames(conn);
//            logger.info("MDB 테이블 개수: {}", tableNames.size());
            
            for (String table : tableNames) {
                try {
                    Map<String, Integer> rStreamValues = getRStreamValues(conn, table);
                    int rStreamCnt = getMdbRStreamCnt(conn, table);

                    result.add(new MdbTableInfo.Builder()
                            .mdbFileName(mdbFileName)
                            .tableName(table)
                            .rStreamValues(rStreamValues)
                            .rStreamCnt(rStreamCnt)
                            .build());
                } catch (Exception e) {
                    logger.error("MDB 테이블 {} 처리 중 오류: {}", table, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR] {} 처리 중 오류: {}", mdb.getName(), e.getMessage(), e);
        }
        return result;
    }
    /**
    * MDB 테이블 set 추출
    */
    private Set<String> getMDbTableNames(Connection conn) throws SQLException {
        Set<String> tables = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    /**
     * MDB R_stream 컬럼에서 중복 제거 및 개수 Map에 정리.
     */
    private HashMap<String, Integer> getRStreamValues(Connection conn, String table) throws SQLException {
        HashMap<String, Integer> rStreamInfo= new HashMap<>();
        List<String> rStreamValues = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();

        // R_stream 컬럼이 있는지 확인
        boolean hasRStreamColumn = false;
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                if ("r_stream".equalsIgnoreCase(colName)) {
                    hasRStreamColumn = true;
                    break;
                }
            }
        }

        if (hasRStreamColumn) {
            String sql = "SELECT DISTINCT [R_stream] FROM [" + table + "] WHERE [R_stream] IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String value = rs.getString(1);
                    if (value != null && !value.trim().isEmpty()) {
                        rStreamValues.add(value.trim());
                    }
                }
            }
            sql = "SELECT COUNT(*) FROM [" + table + "] WHERE [R_stream] = ?";
            for (String rStream : rStreamValues) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, rStream);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            rStreamInfo.put(rStream, count);
                        }
                    }
                }
            }
        }
        return rStreamInfo;
    }

    /**
     * MDB R_stream 개수 리턴
     */
    private int getMdbRStreamCnt(Connection conn, String table) throws SQLException {
        int rStreamFullCnt = 0;
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                if ("r_stream".equalsIgnoreCase(colName)) {
                    String sql = "SELECT COUNT(*) [R_stream] FROM [" + table + "] WHERE [R_stream] IS NOT NULL";
                    try (PreparedStatement ps = conn.prepareStatement(sql);
                         ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            rStreamFullCnt = rs.getInt(1);
                        }
                    }
                }
            }
        }
        return rStreamFullCnt;
    }

    // 예약어, 대문자 테이블 명 처리.
    private String convertTableCorrectName(String table){
        if (UPPER_TABLE.containsKey(table)) {
            return UPPER_TABLE.get(table);
        } else if (RESERVEDWORD.containsKey(table)) {
            return RESERVEDWORD.get(table);
        } else {
            return table;
        }
    }
}