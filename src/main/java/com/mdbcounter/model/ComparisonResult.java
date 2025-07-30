package com.mdbcounter.model;

import org.bouncycastle.asn1.esf.OtherHash;

import java.util.List;
import java.util.Set;

/**
 * MDB와 DB 비교 결과를 담는 모델 클래스
 */
public class ComparisonResult {
    private final List<MissingTableInfo> missingTables;
    private final List<MissingKeyInfo> missingKeys;
    private final List<String> rStreamValues;

    private ComparisonResult(Builder builder) {
        this.missingTables = builder.missingTables;
        this.missingKeys = builder.missingKeys;
        this.rStreamValues = builder.rStreamValues;
    }

    public List<MissingTableInfo> getMissingTables() { return missingTables; }
    public List<MissingKeyInfo> getMissingKeys() { return missingKeys; }
    public List<String> rStreamValues(){return rStreamValues; }

    public static class Builder {
        private List<MissingTableInfo> missingTables;
        private List<MissingKeyInfo> missingKeys;
        private List<String> rStreamValues;

        public Builder missingTables(List<MissingTableInfo> missingTables) {
            this.missingTables = missingTables;
            return this;
        }
        public Builder missingKeys(List<MissingKeyInfo> missingKeys) {
            this.missingKeys = missingKeys;
            return this;
        }
        public Builder getMissingCnt(List<String> rStreamValues) {
            this.rStreamValues = rStreamValues;
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
} 