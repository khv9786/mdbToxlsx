package com.mdbcounter.domain.dto;

public class MissingKeyInfo {
    private final String mdbFileName;
    private final String tableName;
    private final String rStreamValue;

    public MissingKeyInfo(Builder builder) {
        this.mdbFileName = builder.mdbFileName;
        this.tableName = builder.tableName;
        this.rStreamValue = builder.rStreamValue;
    }
    public static class Builder {
        private String mdbFileName;
        private String tableName;
        private String rStreamValue;

        public MissingKeyInfo.Builder mdbFileName(String mdbFileName) { this.mdbFileName = mdbFileName; return this; }
        public MissingKeyInfo.Builder tableName(String tableName) { this.tableName = tableName; return this; }
        public MissingKeyInfo.Builder rStreamValue(String rStreamValue) { this.rStreamValue = rStreamValue; return this; }

        public MissingKeyInfo build() {
            return new MissingKeyInfo(this);
        }
    }
    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public String getRStreamValue() { return rStreamValue; }
}