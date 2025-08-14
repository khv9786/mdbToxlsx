package com.mdbcounter.service;

import com.mdbcounter.domain.dto.CompareCntInfo;
import com.mdbcounter.domain.dto.ComparisonResult;
import com.mdbcounter.domain.dto.MissingKeyInfo;
import com.mdbcounter.domain.dto.MissingTableInfo;
import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.repository.dao.DbComparisonDao;
import com.mdbcounter.repository.dao.DbDao;
import com.mdbcounter.util.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class DbCompareService {

    private static final Logger log = LoggerFactory.getLogger(DbCompareService.class);
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


    private final DbComparisonDao dbComparisonDao;
    private final DbDao dbDao;

    public DbCompareService(DbDao dbDao, DbComparisonDao dbComparisonDao) {
        this.dbComparisonDao = dbComparisonDao;
        this.dbDao = dbDao;
    }

    /**
     * 메인 메서드
     * @param mdbTableInfos
     * @return
     */
    public ComparisonResult compareDbWithMdb(List<MdbTableInfo> mdbTableInfos) {
        log.info("==== 비교 로직 시작 =====");

        try (Connection dbConn = DatabaseConfig.getConnection()) {
            Set<String> dbTables = dbDao.getDbTableName(dbConn);
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
    private ComparisonResult buildComparisonResult(List<MdbTableInfo> mdbTableInfos,
                                                                             Set<String> dbTables,
                                                                             Connection dbConn) throws SQLException {
        List<MissingTableInfo> missingTables = new ArrayList<>();
        List<MissingKeyInfo> missingKeys = new ArrayList<>();
        List<CompareCntInfo> compareCnt = new ArrayList<>();

        for (MdbTableInfo mdbTable : mdbTableInfos) {
            tableComparison(mdbTable, dbTables, dbConn, missingTables, missingKeys, compareCnt);
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
    private void tableComparison(MdbTableInfo mdbTable,
                                 Set<String> dbTables,
                                 Connection dbConn,
                                 List<MissingTableInfo> missingTables,
                                 List<MissingKeyInfo> missingKeys,
                                 List<CompareCntInfo> compareCnt) throws SQLException {

        String normalizedTableName = mdbTable.getNormalizedMdbColName();

        if (isTableMissing(normalizedTableName, dbTables)) {
            handleMissingTable(mdbTable, missingTables);
            return;
        }

        rStreamComparisonLogic(mdbTable, dbConn, missingKeys, compareCnt);
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
    private void handleMissingTable(MdbTableInfo mdbTable, List<MissingTableInfo> missingTables) {
        missingTables.add(new MissingTableInfo.Builder()
                .mdbFileName(mdbTable.getMdbFileName())
                .tableName(mdbTable.getNormalizedMdbColName())
                .build());
    }

    /**
     * RStream 비교 처리
     */
    private void rStreamComparisonLogic(MdbTableInfo mdbTable,
                                        Connection dbConn,
                                        List<MissingKeyInfo> missingKeys,
                                        List<CompareCntInfo> compareCnt) throws SQLException {

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
     * 개별 RStream Entry 처리
     */
    private void processRStreamEntry(MdbTableInfo mdbTable,
                                     Map.Entry<String, Integer> rStreamEntry,
                                     String filterTableName,
                                     Connection dbConn,
                                     List<MissingKeyInfo> missingKeys,
                                     List<CompareCntInfo> compareCnt) throws SQLException {

        String rStream = rStreamEntry.getKey();
        int mdbCount = rStreamEntry.getValue();
        int dbCount = dbComparisonDao.getDbRStreamCnt(dbConn, filterTableName, rStream);

        addCountComparison(mdbTable, rStream, mdbCount, dbCount, compareCnt);

        if (isKeyMissing(dbCount)){
            addMissingKey(mdbTable, rStream, missingKeys);
        }
    }

    /**
     * 카운트 비교 정보 추가
     */
    private void addCountComparison(MdbTableInfo mdbTable, String rStream, int mdbCount, int dbCount,
                                    List<CompareCntInfo> compareCnt) {
        compareCnt.add(new CompareCntInfo.Builder()
                .mdbFileName(mdbTable.getMdbFileName())
                .tableName(mdbTable.getTableName())
                .rStreamValue(rStream)
                .mdbCount(mdbCount)
                .dbCount(dbCount)
                .build());

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
                               List<MissingKeyInfo> missingKeys) {
        missingKeys.add(new MissingKeyInfo.Builder()
                .mdbFileName(mdbTable.getMdbFileName())
                .tableName(mdbTable.getTableName())
                .rStreamValue(rStream)
                .build());
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
     * 예약어, 대문자 테이블명 처리
     */
    private String convertTableCorrectName(String table) {
        if (UPPER_TABLE.containsKey(table)) {
            return UPPER_TABLE.get(table);
        }
        return RESERVEDWORD.getOrDefault(table, table);
    }

    /**
     * Excel 출력 가능한 데이터 존재 여부 확인
     */
    public boolean hasExcelData(ComparisonResult result) {
        return (result.getMissingTables().isEmpty() && result.getMissingKeys().isEmpty());
    }
}