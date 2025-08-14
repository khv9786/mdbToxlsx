package com.mdbcounter.model;

import java.util.Map;

/**
 * MDB 테이블 정보를 담는 모델 클래스
 */
public class MdbTableInfo {
    private final String mdbFileName;
    private final String tableName;
    private final Map<String, Integer> rStreamValues;

    private MdbTableInfo(Builder builder) {
        this.mdbFileName = builder.mdbFileName;
        this.tableName = builder.tableName;
        this.rStreamValues = builder.rStreamValues;
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public Map<String, Integer> getRStreamValues() { return rStreamValues; }

    /**
     * MDB 컬럼명 정규화, 스페이스 -> 언더바, 대문자 -> 소문자 변환
     */
    public String getNormalizedMdbColName() {
        return tableName.replaceAll("\\s+", "_").toLowerCase();
    }

    public static class Builder {
        private String mdbFileName;
        private String tableName;
        private Map<String, Integer> rStreamValues;

        public Builder mdbFileName(String mdbFileName) {this.mdbFileName = mdbFileName; return this;}
        public Builder tableName(String tableName) { this.tableName = tableName; return this;}
        public Builder rStreamValues(Map<String, Integer> rStreamValues) { this.rStreamValues = rStreamValues; return this;}

        public MdbTableInfo build() { return new MdbTableInfo(this);}
    }
}