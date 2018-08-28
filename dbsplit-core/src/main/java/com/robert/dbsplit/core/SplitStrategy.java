package com.robert.dbsplit.core;

public interface SplitStrategy {
    /**
     * 获取节点编号
     *
     * @param splitKey
     * @param <K>
     * @return
     */
    public <K> int getNodeNo(K splitKey);

    /**
     * 获取数据库编号
     *
     * @param splitKey
     * @param <K>
     * @return
     */
    public <K> int getDbNo(K splitKey);

    /**
     * 获取表的编号
     *
     * @param splitKey
     * @param <K>
     * @return
     */

    public <K> int getTableNo(K splitKey);
}
