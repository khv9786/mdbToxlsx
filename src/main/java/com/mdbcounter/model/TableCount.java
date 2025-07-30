package com.mdbcounter.model;

/**
 * 테이블/컬럼별 유효 데이터 개수 정보를 담는 모델 클래스
 * 빌더 패턴을 통해 객체를 생성할 수 있습니다.
 */
public class TableCount {
    /** 테이블명 */
    private final String tableName;
    /** 해당 테이블 컬럼의 유효 데이터 개수 */
    private final int count;
    private final String key;

    private TableCount(Builder builder) {
        this.tableName = builder.tableName;
        this.count = builder.count;
        this.key = builder.key;
    }

    public String getTableName() { return tableName; }
    public int getCount() { return count; }
    public String getKey() {return key; }

    public static class Builder {
        private String tableName;
        private int count;
        private String key;

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        public Builder count(int count) {
            this.count = count;
            return this;
        }
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public TableCount build() {
            return new TableCount(this);
        }
    }
} 