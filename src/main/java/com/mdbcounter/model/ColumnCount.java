package com.mdbcounter.model;

/**
 * 테이블의 특정 컬럼에 대한 데이터 개수 정보를 담는 모델 클래스
 * MDB 파일의 테이블별 컬럼 데이터 개수를 저장하고 전달하는 용도로 사용됩니다.
 */
public class ColumnCount {
    /** 테이블명 */
    private String tableName;
    
    /** 컬럼명 */
    private String columnName;
    
    /** 해당 컬럼의 유효한 데이터 개수 */
    private int count;

    /**
     * ColumnCount 객체를 생성합니다.
     * 
     * @param tableName 테이블명
     * @param columnName 컬럼명
     * @param count 유효한 데이터 개수
     */
    public ColumnCount(String tableName, String columnName, int count) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.count = count;
    }
    
    /**
     * 테이블명을 반환합니다.
     * 
     * @return 테이블명
     */
    public String getTableName() { 
        return tableName; 
    }
    
    /**
     * 컬럼명을 반환합니다.
     * 
     * @return 컬럼명
     */
    public String getColumnName() { 
        return columnName; 
    }
    
    /**
     * 유효한 데이터 개수를 반환합니다.
     * 
     * @return 데이터 개수
     */
    public int getCount() { 
        return count; 
    }
} 