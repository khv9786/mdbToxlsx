package com.mdbcounter.domain.dto;

import java.util.List;

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
    public List<CompareCntInfo> getCompareCnt() { return compareCnt; }

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
}