package com.mdbcounter.model;

/**
 * 테이블/컬럼별 유효 데이터 개수 정보
 */
public class TableCount {

    private final String tableName;
    private final int count;

    private TableCount(Builder builder) {
        this.tableName = builder.tableName;
        this.count = builder.count;
    }

    public String getTableName() {return tableName;}
    public int getCount() {return count;}

    public static class Builder {
        private String tableName;
        private int count;

        public Builder tableName(String tableName) {this.tableName = tableName; return this;}
        public Builder count(int count) {this.count = count; return this;}

        public TableCount build() {return new TableCount(this);}
    }
} 