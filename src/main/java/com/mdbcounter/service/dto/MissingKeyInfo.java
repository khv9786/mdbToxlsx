package com.mdbcounter.service.dto;

public class MissingKeyInfo {
    private final String mdbFileName;
    private final String tableName;
    private final String rStreamValue;

    public MissingKeyInfo(String mdbFileName, String tableName, String rStreamValue) {
        this.mdbFileName = mdbFileName;
        this.tableName = tableName;
        this.rStreamValue = rStreamValue;
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public String getRStreamValue() { return rStreamValue; }
}