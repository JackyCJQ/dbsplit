package com.robert.dbsplit.core;

import java.util.HashMap;
import java.util.List;

/**
 * 分表
 */
public class SplitTablesHolder {
    //分表的切割符
    private static final String DB_TABLE_SEP = "$";
    //切割的表格的集合
    private List<SplitTable> splitTables;

    private HashMap<String, SplitTable> splitTablesMapFull;

    private HashMap<String, SplitTable> splitTablesMap;

    public SplitTablesHolder() {

    }

    public SplitTablesHolder(List<SplitTable> splitTables) {
        this.splitTables = splitTables;

        init();
    }

    public void init() {
        splitTablesMapFull = new HashMap<String, SplitTable>();
        splitTablesMap = new HashMap<String, SplitTable>();

        for (int i = 0; i < splitTables.size(); i++) {
            SplitTable st = splitTables.get(i);

            String key = constructKey(st.getDbNamePrefix(), st.getTableNamePrefix());
            splitTablesMapFull.put(key, st);

            splitTablesMap.put(st.getTableNamePrefix(), st);
        }
    }

    /**
     * dbName + & + tableName
     *
     * @param dbName
     * @param tableName
     * @return
     */
    private String constructKey(String dbName, String tableName) {
        return dbName + DB_TABLE_SEP + tableName;
    }

    /**
     * 获取对应的SplitTable信息
     *
     * @param dbName
     * @param tableName
     * @return
     */
    public SplitTable searchSplitTable(String dbName, String tableName) {
        return splitTablesMapFull.get(constructKey(dbName, tableName));
    }

    public SplitTable searchSplitTable(String tableName) {
        return splitTablesMap.get(tableName);
    }

    public List<SplitTable> getSplitTables() {
        return splitTables;
    }

    public void setSplitTables(List<SplitTable> splitTables) {
        this.splitTables = splitTables;
    }
}
