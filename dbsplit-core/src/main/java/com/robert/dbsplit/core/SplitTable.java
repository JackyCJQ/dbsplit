package com.robert.dbsplit.core;

import java.util.List;

/**
 * 分表的详细信息
 */
public class SplitTable {
    //数据库的前缀
    private String dbNamePrefix;
    //数据表的前缀
    private String tableNamePrefix;
    //数据库的编号
    private int dbNum;
    //数据表的编号
    private int tableNum;
    //默认是垂直分表
    private SplitStrategyType splitStrategyType = SplitStrategyType.VERTICAL;

    private SplitStrategy splitStrategy;
    private List<SplitNode> splitNodes;
    //默认是读写分离的
    private boolean readWriteSeparate = true;

    public void init() {
        if (splitStrategyType == SplitStrategyType.VERTICAL)
            this.splitStrategy = new VerticalHashSplitStrategy(
                    splitNodes.size(), dbNum, tableNum);
        else if (splitStrategyType == SplitStrategyType.HORIZONTAL)
            this.splitStrategy = new HorizontalHashSplitStrategy(
                    splitNodes.size(), dbNum, tableNum);
    }

    public void setSplitStrategyType(String splitStrategyType) {
        this.splitStrategyType = SplitStrategyType.valueOf(splitStrategyType);
    }

    public String getDbNamePrefix() {
        return dbNamePrefix;
    }

    public void setDbNamePrefix(String dbNamePrifix) {
        this.dbNamePrefix = dbNamePrifix;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrifix) {
        this.tableNamePrefix = tableNamePrifix;
    }

    public int getDbNum() {
        return dbNum;
    }

    public void setDbNum(int dbNum) {
        this.dbNum = dbNum;
    }

    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }

    public List<SplitNode> getSplitNodes() {
        return splitNodes;
    }

    public void setSplitNodes(List<SplitNode> splitNodes) {
        this.splitNodes = splitNodes;
    }

    public SplitStrategy getSplitStrategy() {
        return splitStrategy;
    }

    public void setSplitStrategy(SplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
    }

    public boolean isReadWriteSeparate() {
        return readWriteSeparate;
    }

    public void setReadWriteSeparate(boolean readWriteSeparate) {
        this.readWriteSeparate = readWriteSeparate;
    }
}
