package com.mdbcounter.domain.dto;


public class MissingTableInfo {
    private final String mdbFileName;
    private final String tableName;

    public MissingTableInfo(String mdbFileName, String tableName) {
        this.mdbFileName = mdbFileName;
        this.tableName = tableName;
    }

    public String getMdbFileName() { return mdbFileName; }
    public String getTableName() { return tableName; }
}
