package com.mdbcounter.service.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.mdbcounter.service.dao.MdbDao.COL_NAME;

public class DbComparisonDao {

    /**
     * 특정 r_stream가 DB에 몇개가 있는지 조회
     */
    public int getDbRStreamCnt(Connection conn, String tableName, String rStream) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + COL_NAME + " = ?";
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
}