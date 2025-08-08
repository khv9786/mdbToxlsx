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
    public static final String COLNAME = "r_stream";
    private static final Logger log = LoggerFactory.getLogger(DbComparisonService.class);
    private static final Map<String, String> UPPER_TABLE;
    private static final Map<String, String> RESERVEDWORD;

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

    static {
        Map<String, String> map = new HashMap<>();
        map.put("cross", "\"cross\"");
        map.put("CROSS", "\"cross\"");
        RESERVEDWORD = Collections.unmodifiableMap(map);
    }

//    /**
//     * MDB와 DB를 비교하여 결과를 반환
//     *
//     * @param mdbTableInfos MDB 테이블 정보 리스트
//     * @return 비교 결과
//     */
//    public ComparisonResult compareMdbWithDbOptimized(List<MdbTableInfo> mdbTableInfos) {
//        log.info("==== 비교 로직 시작 =====");
//        List<ComparisonResult.MissingTableInfo> missingTables = new ArrayList<>();
//        List<ComparisonResult.MissingKeyInfo> missingKeys = new ArrayList<>();
//        List<ComparisonResult.CompareCntInfo> compareCnt = new ArrayList<>();
//
//        try (Connection dbConn = DatabaseConfig.getConnection()) {
//            //  DB에 존재하는 테이블 목록 정리.
//            Set<String> dbTables = getDbTables(dbConn);
//            log.info("DB에 존재하는 테이블 개수: {}", dbTables.size());
//            log.info("MDB내 테이블 개수: {}", mdbTableInfos.size());
//            for (MdbTableInfo mdbTable : mdbTableInfos) {
//
//                // MDB 테이블 명 정리.
//                String mdbFileName = mdbTable.getMdbFileName(); // MDB 파일이름
//                String tableName = mdbTable.getTableName(); // MDB 테이블 이름
//                String NormalizedTableName = mdbTable.getNormalizedMdbColName(); // 정규화한 테이블 이름
//
//                if (!dbTables.contains(NormalizedTableName)) {
//                    missingTables.add(new ComparisonResult.MissingTableInfo(mdbFileName, NormalizedTableName));
//                    continue;
//                }
//                String fillterTableName = convertTableCorrectName(NormalizedTableName);
//                Map<String, Integer> mdbRStreams = mdbTable.getRStreamValues();
//
//                if (!mdbRStreams.isEmpty()) {
//
//                    Iterator<String> keys = mdbRStreams.keySet().iterator();
//                    while (keys.hasNext()) {
//                        String rStream = keys.next();
//
//                        int mdbCount = mdbRStreams.get(rStream);
//                        int dbCount = getRStreamCntDb(dbConn, fillterTableName, rStream);
//
//                        compareCnt.add(new ComparisonResult.CompareCntInfo(
//                            mdbFileName, tableName, rStream, mdbCount, dbCount));
//
//                        if (dbCount == 0) {
//                            missingKeys.add(new ComparisonResult.MissingKeyInfo(mdbTable.getMdbFileName(), tableName, rStream));
//                        }
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            log.error("DB 비교 중 오류: {}", e.getMessage(), e);
//        }
//
//        return new ComparisonResult.Builder()
//                .missingTables(missingTables)
//                .missingKeys(missingKeys)
//                .compareCnt(compareCnt)
//                .build();
//    }
    // TODO 단계 별로 메서드 분리 완료. 인자 이름이 길어서 다소 불편,,, 더 좋은 방법 찾아보긴 해야할듯.
    /**
     * 메인 비교 메서드 - 전체 흐름 관리
     */
    public ComparisonResult compareDbWithMdb(List<MdbTableInfo> mdbTableInfos) {
        log.info("==== 비교 로직 시작 =====");

        try (Connection dbConn = DatabaseConfig.getConnection()) {
            Set<String> dbTables = getDbTables(dbConn);
            logTableCounts(dbTables, mdbTableInfos);

            return buildComparisonResult(mdbTableInfos, dbTables, dbConn);

        } catch (SQLException e) {
            log.error("DB 비교 중 오류: {}", e.getMessage(), e);
            return createEmptyResult();
        }
    }

    /**
     * 비교 결과 구성
     */
    private ComparisonResult buildComparisonResult(List<MdbTableInfo> mdbTableInfos, Set<String> dbTables, Connection dbConn) throws SQLException {
        List<ComparisonResult.MissingTableInfo> missingTables = new ArrayList<>();
        List<ComparisonResult.MissingKeyInfo> missingKeys = new ArrayList<>();
        List<ComparisonResult.CompareCntInfo> compareCnt = new ArrayList<>();

        for (MdbTableInfo mdbTable : mdbTableInfos) {
            TableComparison(mdbTable, dbTables, dbConn, missingTables, missingKeys, compareCnt);
        }

        return new ComparisonResult.Builder()
                .missingTables(missingTables)
                .missingKeys(missingKeys)
                .compareCnt(compareCnt)
                .build();
    }

    /**
     * 개별 테이블 비교 처리
     */
    private void TableComparison(MdbTableInfo mdbTable, Set<String> dbTables, Connection dbConn,
                                 List<ComparisonResult.MissingTableInfo> missingTables,
                                 List<ComparisonResult.MissingKeyInfo> missingKeys,
                                 List<ComparisonResult.CompareCntInfo> compareCnt) throws SQLException {

        // 이름 정규화
        String normalizedTableName = mdbTable.getNormalizedMdbColName();

        if (isTableMissing(normalizedTableName, dbTables)) {
            handleMissingTable(mdbTable, missingTables);
            return;
        }

        rStreamComparisonlogic(mdbTable, dbConn, missingKeys, compareCnt);
    }

    /**
     * 테이블 존재 여부 확인
     */
    private boolean isTableMissing(String normalizedTableName, Set<String> dbTables) {
        return !dbTables.contains(normalizedTableName);
    }

    /**
     * 누락된 테이블 처리
     */
    private void handleMissingTable(MdbTableInfo mdbTable, List<ComparisonResult.MissingTableInfo> missingTables) {
        missingTables.add(new ComparisonResult.MissingTableInfo(mdbTable.getMdbFileName(), mdbTable.getNormalizedMdbColName()
        ));
    }

    /**
     * RStream 비교 처리
     */
    private void rStreamComparisonlogic(MdbTableInfo mdbTable, Connection dbConn,
                                        List<ComparisonResult.MissingKeyInfo> missingKeys,
                                        List<ComparisonResult.CompareCntInfo> compareCnt) throws SQLException {

        Map<String, Integer> mdbRStreams = mdbTable.getRStreamValues();
        if (mdbRStreams.isEmpty()) {
            return;
        }

        String filterTableName = convertTableCorrectName(mdbTable.getNormalizedMdbColName());

        for (Map.Entry<String, Integer> entry : mdbRStreams.entrySet()) {
            processRStreamEntry(mdbTable, entry, filterTableName, dbConn, missingKeys, compareCnt);
        }
    }

    /**
     * 개별 RStream Map Entry로 정리
     */
    private void processRStreamEntry(MdbTableInfo mdbTable, Map.Entry<String, Integer> rStreamEntry, String filterTableName, Connection dbConn,
                                     List<ComparisonResult.MissingKeyInfo> missingKeys,
                                     List<ComparisonResult.CompareCntInfo> compareCnt) throws SQLException {

        String rStream = rStreamEntry.getKey();
        int mdbCount = rStreamEntry.getValue();
        int dbCount = getRStreamCntDb(dbConn, filterTableName, rStream);

        addCountComparison(mdbTable, rStream, mdbCount, dbCount, compareCnt);

        if (isKeyMissing(dbCount)) {
            addMissingKey(mdbTable, rStream, missingKeys);
        }
    }

    /**
     * 카운트 비교 정보 추가
     */
    private void addCountComparison(MdbTableInfo mdbTable, String rStream, int mdbCount, int dbCount,
                                    List<ComparisonResult.CompareCntInfo> compareCnt) {
        compareCnt.add(new ComparisonResult.CompareCntInfo(
                mdbTable.getMdbFileName(),
                mdbTable.getTableName(),
                rStream,
                mdbCount,
                dbCount
        ));
    }

    /**
     * 키 누락 여부 확인
     */
    private boolean isKeyMissing(int dbCount) {
        return dbCount == 0;
    }

    /**
     * 누락된 키 정보 추가
     */
    private void addMissingKey(MdbTableInfo mdbTable, String rStream,
                               List<ComparisonResult.MissingKeyInfo> missingKeys) {
        missingKeys.add(new ComparisonResult.MissingKeyInfo(
                mdbTable.getMdbFileName(),
                mdbTable.getTableName(),
                rStream
        ));
    }

    /**
     * 테이블 개수 로깅
     */
    private void logTableCounts(Set<String> dbTables, List<MdbTableInfo> mdbTableInfos) {
        log.info("DB에 존재하는 테이블 개수: {}", dbTables.size());
        log.info("MDB내 테이블 개수: {}", mdbTableInfos.size());
    }

    /**
     * 빈 결과 생성 (에러 발생 시)
     */
    private ComparisonResult createEmptyResult() {
        return new ComparisonResult.Builder()
                .missingTables(new ArrayList<>())
                .missingKeys(new ArrayList<>())
                .compareCnt(new ArrayList<>())
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
    private int getRStreamCntDb(Connection conn, String tableName, String rStream) throws SQLException {
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
                    Map<String, Integer> rStreamValues = getMdbRStreamValues(conn, table);
                    result.add(new MdbTableInfo.Builder()
                            .mdbFileName(mdbFileName)
                            .tableName(table)
                            .rStreamValues(rStreamValues)
//                            .rStreamCnt(rStreamCnt)
                            .build());
                } catch (Exception e) {
                    log.error("MDB 테이블 {} 처리 중 오류: {}", table, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[ERROR] {} 처리 중 오류: {}", mdb.getName(), e.getMessage(), e);
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
     * MDB R_stream 컬럼에서 중복 제거 및 해당 키의 개수 Map에 정리.
     */
    private HashMap<String, Integer> getMdbRStreamValues(Connection conn, String table) throws SQLException {
        HashMap<String, Integer> rStreamInfo = new HashMap<>();
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
        // 튜닝 후 N+1 문제 해결
        if (hasRStreamColumn) {
            String sql = "SELECT R_stream, COUNT(*) as cnt FROM [" + table + "] WHERE R_stream IS NOT NULL GROUP BY R_stream";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rStream = rs.getString("R_stream").trim();
                    int count = rs.getInt("cnt");
                    if (!rStream.isEmpty()) {
                        rStreamInfo.put(rStream, count);
                    }
                }
            }
        }
        return rStreamInfo;
    }

    /**
     * mdb 데이터 로딩
     */
    public List<MdbTableInfo> loadMdbData(List<File> mdbFiles) {

        List<MdbTableInfo> allMdbTableInfos = new ArrayList<>();
        for (File mdb : mdbFiles) {
            List<MdbTableInfo> oneFileInfos = getMdbTableInfo(mdb);
            allMdbTableInfos.addAll(oneFileInfos);
        }

        return allMdbTableInfos;
    }

    /**
     *     예약어, 대문자 테이블 명 처리.
      */

    private String convertTableCorrectName(String table) {
        if (UPPER_TABLE.containsKey(table)) {
            return UPPER_TABLE.get(table);
        } else if (RESERVEDWORD.containsKey(table)) {
            return RESERVEDWORD.get(table);
        } else {
            return table;
        }
    }

    public boolean isDatathere(ComparisonResult result){
        return (result.getMissingTables().isEmpty() && result.getMissingKeys().isEmpty());
    }
}