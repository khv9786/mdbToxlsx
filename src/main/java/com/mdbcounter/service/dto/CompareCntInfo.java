package com.mdbcounter.service.dto;


public class CompareCntInfo {
    private final String mdbFileName;
    private final String tableName;
    private final String rStreamValue;
    private final int mdbCount;
    private final int dbCount;
    private final int difference;

    public CompareCntInfo(String mdbFileName, String tableName, String rStreamValue,
                          int mdbCount, int dbCount) {
        this.mdbFileName = mdbFileName;
        this.tableName = tableName;
        this.rStreamValue = rStreamValue;
        this.mdbCount = mdbCount;
        this.dbCount = dbCount;
        this.difference = mdbCount - dbCount;
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
    public String getRStreamValue() { return rStreamValue; }
    public int getMdbCount() { return mdbCount; }
    public int getDbCount() { return dbCount; }
    public int getDifference() { return difference; }
}