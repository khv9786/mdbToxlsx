package com.mdbcounter.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 데이터베이스 연결 및 테이블 정보를 확인하는 유틸리티 클래스
 */
public class DatabaseTest {
    
    /**
     * 데이터베이스 연결을 테스트합니다.
     */
    public static void testDatabaseConnection() {
        System.out.println("=== PostgreSQL 데이터베이스 연결 테스트 ===");
        
        if (DatabaseConfig.testConnection()) {
            System.out.println("✅ 데이터베이스 연결 성공!");
            System.out.println("데이터베이스 URL: " + DatabaseConfig.getDatabaseUrl());
            System.out.println("사용자명: " + DatabaseConfig.getUsername());
            
            // 테이블 목록 출력
            printTableList();
        } else {
            System.err.println("❌ 데이터베이스 연결 실패!");
            System.err.println("database.properties 파일의 설정을 확인해주세요.");
        }
    }
    
    /**
     * 데이터베이스의 모든 테이블 목록을 출력합니다.
     */
    public static void printTableList() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sb = new StringBuilder();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("\n📋 데이터베이스 테이블 목록:");
            System.out.println("----------------------------------------");
            
            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
                sb.append("테이블: ").append(tableName).append("\n");
            }
            System.out.println(sb);
            if (tableNames.isEmpty()) {
                System.out.println("테이블이 없습니다.");
            } else {
                System.out.println("----------------------------------------");
                System.out.println("총 " + tableNames.size() + "개의 테이블이 있습니다.");
            }
            
        } catch (SQLException e) {
            System.err.println("테이블 목록 조회 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 특정 테이블의 컬럼 정보를 출력합니다.
     * @param tableName 테이블명
     */
    public static void printTableColumns(String tableName) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            
            System.out.println("\n📋 테이블 '" + tableName + "' 컬럼 정보:");
            System.out.println("----------------------------------------");
            
            int columnCount = 0;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                
                System.out.printf("컬럼: %-20s | 타입: %-15s | 크기: %-5d | NULL: %s%n", 
                    columnName, dataType, columnSize, isNullable);
                columnCount++;
            }
            
            System.out.println("----------------------------------------");
            System.out.println("총 " + columnCount + "개의 컬럼이 있습니다.");
            
        } catch (SQLException e) {
            System.err.println("컬럼 정보 조회 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 메인 메서드 - 직접 실행하여 데이터베이스 연결을 테스트할 수 있습니다.
     */
    public static void main(String[] args) {
        testDatabaseConnection();

        // printTableColumns("");
    }
} 