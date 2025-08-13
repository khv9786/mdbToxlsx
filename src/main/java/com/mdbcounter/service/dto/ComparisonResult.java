package com.mdbcounter.service.dto;

import java.util.List;

public class ComparisonResult {
    private final List<MissingTableInfo> missingTables;
    private final List<MissingKeyInfo> missingKeys;
    private final List<CompareCntInfo> compareCnt;

    public ComparisonResult(List<MissingTableInfo> missingTables,
                            List<MissingKeyInfo> missingKeys,
                            List<CompareCntInfo> compareCnt) {
        this.missingTables = missingTables;
        this.missingKeys = missingKeys;
        this.compareCnt = compareCnt;
    }

    public List<MissingTableInfo> getMissingTables() { return missingTables; }
    public List<MissingKeyInfo> getMissingKeys() { return missingKeys; }
    public List<CompareCntInfo> getCompareCnt() { return compareCnt; }
}