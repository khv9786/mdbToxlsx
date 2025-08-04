package com.mdbcounter.model;

import java.util.List;

/**
 * DB 테이블 정보를 담는 모델 클래스
 */
public class DbTableInfo {
    private final String tableName;
    private final List<String> rStreamValues;
    private final int totalCount;

    private DbTableInfo(Builder builder) {
        this.tableName = builder.tableName;
        this.rStreamValues = builder.rStreamValues;
        this.totalCount = builder.totalCount;
    }

    public String getTableName() { return tableName; }
    public List<String> getRStreamValues() { return rStreamValues; }
    public int getTotalCount() { return totalCount; }

    public static class Builder {
        private String tableName;
        private List<String> rStreamValues;
        private int totalCount;

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        public Builder rStreamValues(List<String> rStreamValues) {
            this.rStreamValues = rStreamValues;
            return this;
        }
        public Builder totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        public DbTableInfo build() {
            return new DbTableInfo(this);
        }
    }
} 