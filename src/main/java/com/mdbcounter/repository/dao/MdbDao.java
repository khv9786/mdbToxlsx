package com.mdbcounter.repository.dao;

import com.mdbcounter.model.MdbTableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MdbDao {
    public static final String COL_NAME = "r_stream";
    private static final Logger log = LoggerFactory.getLogger(MdbDao.class);

    /**
     * MDB 파일에서 테이블 정보와 R_stream 값을 읽어서 반환
     *
     * @param mdb MDB 파일
     * @return MDB 테이블 정보 리스트
     */
    public List<MdbTableInfo> getMdbTableInfo(File mdb)  {
        List<MdbTableInfo> result = new ArrayList<>();
        String url = "jdbc:ucanaccess://" + mdb.getAbsolutePath();
        String mdbFileName = mdb.getName().replaceAll("\\.mdb$", ""); // .mdb 확장자 제거

        try (Connection conn = DriverManager.getConnection(url)) { // Try-with-resources
            List<String> tableNames = getMDbTableName(conn);
//            logger.info("MDB 테이블 개수: {}", tableNames.size());

            for (String table : tableNames) {
                try {
                    Map<String, Integer> rStreamValues = getColDataWithCnt(conn, table);
                    result.add(new MdbTableInfo.Builder()
                            .mdbFileName(mdbFileName)
                            .tableName(table)
                            .rStreamValues(rStreamValues)
//                            .rStreamCnt(rStreamCnt)
                            .build());
                } catch (Exception e) {log.error("MDB 테이블 {} 처리 중 오류: {}", table, e.getMessage());}
            }
        } catch (Exception e) {log.error("[ERROR] {} 처리 중 오류: {}", mdb.getName(), e.getMessage(), e);}
        return result;
    }

    /**
     * MDB 테이블 List 추출
     */
    public List<String> getMDbTableName(Connection conn) throws SQLException {
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
     * MDB 파일의 특정 컬럼의 값과 해당 값 Cnt 반환
     *
     * @param conn
     * @param table
     * @return HashMap rStreamInfo
     * @throws SQLException
     */
    private HashMap<String, Integer> getColDataWithCnt(Connection conn, String table) throws SQLException {
        HashMap<String, Integer> rStreamInfo = new HashMap<>();
        DatabaseMetaData meta = conn.getMetaData();

        // R_stream 컬럼이 있는지 확인
        boolean hasRStreamColumn = false;
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) {
            while (cols.next()) {
                String colName = cols.getString("COLUMN_NAME");
                if (COL_NAME.equalsIgnoreCase(colName)) {
                    hasRStreamColumn = true;
                    break;
                }
            }
        }
        // 그룹바이로 한번에 받아오기
        if (hasRStreamColumn) {
            String sql = " SELECT " + COL_NAME + ", COUNT(*) as cnt FROM [" + table + "] WHERE " + COL_NAME + " IS NOT NULL GROUP BY " + COL_NAME;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String rStream = rs.getString(COL_NAME).trim();
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
     * 특정 테이블에 있는 데이터 싹~~~ 끌어오기
     *
     * @param conn
     * @param table
     * @return
     * @throws SQLException
     */
    public int getAllTableData(Connection conn, String table) throws SQLException {
        List<String> columns = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet cols = meta.getColumns(null, null, table, "%")) { // ResultSet 은 자동으로 close.
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
}
