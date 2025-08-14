package com.mdbcounter.domain.dto;


public class MissingTableInfo {
    private final String mdbFileName;
    private final String tableName;

    public MissingTableInfo(Builder builder) {
        this.mdbFileName = builder.mdbFileName;
        this.tableName = builder.tableName;
    }
    public static class Builder {
        private String mdbFileName;
        private String tableName;

        public MissingTableInfo.Builder mdbFileName(String mdbFileName) { this.mdbFileName = mdbFileName; return this; }
        public MissingTableInfo.Builder tableName(String tableName) { this.tableName = tableName; return this; }

        public MissingTableInfo build() {
            return new MissingTableInfo(this);
        }
    }
    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
}
