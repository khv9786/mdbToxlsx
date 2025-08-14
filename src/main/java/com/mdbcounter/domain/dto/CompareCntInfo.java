package com.mdbcounter.domain.dto;


public class CompareCntInfo {
    private final String mdbFileName;
    private final String tableName;
    private final String rStreamValue;
    private final int mdbCount;
    private final int dbCount;
    private final int difference;


    private CompareCntInfo(Builder builder) {
        this.mdbFileName = builder.mdbFileName;
        this.tableName = builder.tableName;
        this.rStreamValue = builder.rStreamValue;
        this.mdbCount = builder.mdbCount;
        this.dbCount = builder.dbCount;
        this.difference = builder.mdbCount - builder.dbCount;
    }

    public static class Builder {
        private String mdbFileName;
        private String tableName;
        private String rStreamValue;
        private int mdbCount;
        private int dbCount;

        public Builder mdbFileName(String mdbFileName) { this.mdbFileName = mdbFileName; return this; }
        public Builder tableName(String tableName) { this.tableName = tableName; return this; }
        public Builder rStreamValue(String rStreamValue) { this.rStreamValue = rStreamValue; return this; }
        public Builder mdbCount(int mdbCount) { this.mdbCount = mdbCount; return this; }
        public Builder dbCount(int dbCount) { this.dbCount = dbCount; return this; }

        public CompareCntInfo build() {
            return new CompareCntInfo(this);
        }
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public String getRStreamValue() { return rStreamValue; }
    public int getMdbCount() { return mdbCount; }
    public int getDbCount() { return dbCount; }
    public int getDifference() { return difference; }


}