package com.robert.dbsplit.core;

/**
 * 两种切分的方式 水平切分和垂直切分
 */
public enum SplitStrategyType {
    VERTICAL("vertical"), HORIZONTAL("horizontal");

    private String value;

    SplitStrategyType(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
