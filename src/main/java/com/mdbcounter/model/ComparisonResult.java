package com.mdbcounter.model;
import java.util.List;

/**
 * MDB와 DB 비교 결과를 담는 모델 클래스
 */
public class ComparisonResult {
    private final List<MissingTableInfo> missingTables;
    private final List<MissingKeyInfo> missingKeys;
    private final List<CompareCntInfo> compareCnt;

    private ComparisonResult(Builder builder) {
        this.missingTables = builder.missingTables;
        this.missingKeys = builder.missingKeys;
        this.compareCnt = builder.compareCnt;
    }

    public List<MissingTableInfo> getMissingTables() { return missingTables; }
    public List<MissingKeyInfo> getMissingKeys() { return missingKeys; }
    public List<CompareCntInfo> getcompareCnt() { return compareCnt; }

    public static class Builder {
        private List<MissingTableInfo> missingTables;
        private List<MissingKeyInfo> missingKeys;
        private List<CompareCntInfo> compareCnt;


        public Builder missingTables(List<MissingTableInfo> missingTables) {
            this.missingTables = missingTables;
            return this;
        }
        public Builder missingKeys(List<MissingKeyInfo> missingKeys) {
            this.missingKeys = missingKeys;
            return this;
        }
        public Builder compareCnt(List<CompareCntInfo> compareCnt) {
            this.compareCnt = compareCnt;
            return this;
        }

        public ComparisonResult build() {
            return new ComparisonResult(this);
        }
    }

    /**
     * 없는 테이블 정보
     */
    public static class MissingTableInfo {
        private final String mdbFileName;
        private final String tableName;


        public MissingTableInfo(String mdbFileName, String tableName) {
            this.mdbFileName = mdbFileName;
            this.tableName = tableName;
        }

        public String getMdbFileName() { return mdbFileName; }
        public String getTableName() { return tableName; }
    }

    /**
     * 없는 키 정보
     */
    public static class MissingKeyInfo {
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

    /**
     * 개수 비교 정보
     */
    public static class CompareCntInfo {
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
} 