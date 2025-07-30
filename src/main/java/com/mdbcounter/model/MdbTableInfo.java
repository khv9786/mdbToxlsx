package com.mdbcounter.model;

import java.util.Set;

/**
 * MDB 테이블 정보를 담는 모델 클래스
 */
public class MdbTableInfo {
    private final String mdbFileName;
    private final String tableName;
    private final Set<String> rStreamValues;
    private final int totalCount;

    private MdbTableInfo(Builder builder) {
        this.mdbFileName = builder.mdbFileName;
        this.tableName = builder.tableName;
        this.rStreamValues = builder.rStreamValues;
        this.totalCount = builder.totalCount;
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public Set<String> getRStreamValues() { return rStreamValues; }
    public int getTotalCount() { return totalCount; }

    /**
     * MDB 컬럼명 정규화, 스페이스 -> 언더바, 대문자 -> 소문자 변환
     */
    public String getNormalizedMdbColName() {
        return tableName.replaceAll("\\s+", "_").toLowerCase();
    }

    public static class Builder {
        private String mdbFileName;
        private String tableName;
        private Set<String> rStreamValues;
        private int totalCount;

        public Builder mdbFileName(String mdbFileName) {
            this.mdbFileName = mdbFileName;
            return this;
        }
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        public Builder rStreamValues(Set<String> rStreamValues) {
            this.rStreamValues = rStreamValues;
            return this;
        }
        public Builder totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        public MdbTableInfo build() {
            return new MdbTableInfo(this);
        }
    }
} 