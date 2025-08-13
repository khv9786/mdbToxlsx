package com.mdbcounter.service;


import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.model.TableCount;
import com.mdbcounter.repository.dao.MdbDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class MdbService {
    public static final String MDB_EXT = ".mdb";
    public static final String COL_NAME = "r_stream";
    private static final Logger log = LoggerFactory.getLogger(MdbService.class);

    private final MdbDao mdbDao;

    public MdbService(MdbDao mdbDao) {
        this.mdbDao = mdbDao;
    }

    /**
     * 각 테이블의 총 데이터 개수를 반환
     *
     * @param mdb
     * @return 테이블별 총 데이터 개수 List<TableCount>
     */
    public List<TableCount> getMdbTableCnt(File mdb) throws SQLException {
        List<TableCount> result = new ArrayList<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url)) {
            for (String table : mdbDao.getMDbTableName(conn)) {
                int totalCount = mdbDao.getAllTableData(conn, table);
                result.add(new TableCount.Builder()
                        .tableName(table)
                        .count(totalCount)
                        .build());
            }
        } catch (SQLException e) {
            log.error("MDB 파일 처리 실패: {}", mdb.getName(), e);
            throw new SQLException("MDB 파일 처리 중 오류 발생: " + mdb.getName(), e);
        }

        return result;
    }

    /**
     * 여러 MDB 파일에서 테이블별로 null이 아닌 값들의 총합 계산
     *
     * @param mdbFiles MDB 파일 리스트
     * @return 테이블별 총 데이터 개수 맵
     */
    public Map<String, Integer> setMdbTableCnt(List<File> mdbFiles) {
        Map<String, Integer> tableTotalMap = new LinkedHashMap<>();
        List<String> failedFiles = new ArrayList<>();

        for (File mdbFile : mdbFiles) {
            try {
                List<TableCount> fileCounts = getMdbTableCnt(mdbFile);
                aggregateTableCounts(tableTotalMap, fileCounts);

            } catch (SQLException e) {
                log.error("파일 처리 실패: {}", mdbFile.getName(), e);
                failedFiles.add(mdbFile.getName());
            }
        }

        if (!failedFiles.isEmpty()) {
            log.warn("처리 실패한 파일들: {}", failedFiles);
        }

        return tableTotalMap;
    }

    /**
     * 컬럼 데이터 더해서 넣기.
     * @param totalMap
     * @param fileCounts
     */
    private void aggregateTableCounts(Map<String, Integer> totalMap, List<TableCount> fileCounts) {
        for (TableCount tableCount : fileCounts) {
            String tableName = tableCount.getTableName();
            int currentTotal = totalMap.getOrDefault(tableName, 0);
            totalMap.put(tableName, currentTotal + tableCount.getCount());
        }
    }

    /**
     * mdb에서 테이블에 null 아닌 값 모두 정리.
     *
     * @param mdbFiles
     * @return
     */
    public Map<String, Integer> calMdbTableCnt(List<File> mdbFiles) throws SQLException {
        Map<String, Integer> tableTotalMap = new LinkedHashMap<>();
        for (File mdb : mdbFiles) {
            List<TableCount> oneFileCounts = getMdbTableCnt(mdb);
            for (TableCount tableCount : oneFileCounts) {
                String tableName = tableCount.getTableName();
                int currentCount = tableTotalMap.getOrDefault(tableName, 0);
                tableTotalMap.put(tableName, currentCount + tableCount.getCount());
            }
        }
        return tableTotalMap;
    }

    /**
     * mdb 데이터 로딩
     */
    public List<MdbTableInfo> loadMdbData(List<File> mdbFiles)  {

        List<MdbTableInfo> allMdbTableInfos = new ArrayList<>();
        for (File mdb : mdbFiles) {
            List<MdbTableInfo> oneFileInfos = mdbDao.getMdbTableInfo(mdb);
            allMdbTableInfos.addAll(oneFileInfos);
        }

        return allMdbTableInfos;
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
            allTableCounts.add(new TableCount.Builder()
                    .tableName(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }
        return allTableCounts;
    }
}