package com.mdbcounter.model;

public class ColumnCount {
    /** 테이블명 */
    private final String tableName;
    /** 컬럼명 */
    private final String columnName;
    /** 해당 컬럼의 유효한 데이터 개수 */
    private final int count;

    private ColumnCount(Builder builder) {
        this.tableName = builder.tableName;
        this.columnName = builder.columnName;
        this.count = builder.count;
    }

    public String getTableName() { return tableName; }
    public String getColumnName() { return columnName; }
    public int getCount() { return count; }

    public static class Builder {
        private String tableName;
        private String columnName;
        private int count;

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        public Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }
        public Builder count(int count) {
            this.count = count;
            return this;
        }
        public ColumnCount build() {
            return new ColumnCount(this);
        }
    }
} 