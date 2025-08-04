package com.mdbcounter.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * PostgreSQL 데이터베이스 연결을 관리하는 설정 클래스
 */
public class DatabaseConfig {
    private static final String CONFIG_FILE = "database.properties";
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("데이터베이스 설정 파일을 찾을 수 없습니다: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("데이터베이스 설정 파일 로드 중 오류: " + e.getMessage(), e);
        }
    }
    
    /**
     * 데이터베이스 연결을 반환합니다.
     * @return Connection 객체
     * @throws SQLException 연결 실패 시
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(properties.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL 드라이버를 찾을 수 없습니다.", e);
        }
        
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        
        return DriverManager.getConnection(url, username, password);
    }

    public static String getDatabaseUrl() {
        return properties.getProperty("db.url");
    }
    public static String getUsername() {
        return properties.getProperty("db.username");
    }
    public static String getPassword() {
        return properties.getProperty("db.password");
    }
    
    /**
     * 데이터베이스 연결을 테스트합니다.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("데이터베이스 연결 테스트 실패: " + e.getMessage());
            return false;
        }
    }
} 