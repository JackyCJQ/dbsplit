package com.robert.dbsplit.core;

/**
 * 水平hash分表策略
 */
public class HorizontalHashSplitStrategy implements SplitStrategy {
    //端口编号
    private int portNum;
    //数据库编号
    private int dbNum;
    //数据表的编号
    private int tableNum;

    public HorizontalHashSplitStrategy() {

    }

    public HorizontalHashSplitStrategy(int portNum, int dbNum, int tableNum) {
        this.portNum = portNum;
        this.dbNum = dbNum;
        this.tableNum = tableNum;
    }

    /**
     * 获取对应的节点编号
     * @param splitKey
     * @return
     */
    public int getNodeNo(Object splitKey) {
        return getDbNo(splitKey) / dbNum;
    }

    /**
     * 获取对应的数据库编号
     * @param splitKey
     * @return
     */
    public int getDbNo(Object splitKey) {
        return getTableNo(splitKey) / tableNum;
    }

    /**
     * 获取对应的数据表的编号
     * @param splitKey
     * @return
     */
    public int getTableNo(Object splitKey) {
        int hashCode = calcHashCode(splitKey);
        return hashCode % (portNum * dbNum * tableNum);
    }

    private int calcHashCode(Object splitKey) {
        int hashCode = splitKey.hashCode();
        if (hashCode < 0)
            hashCode = -hashCode;

        return hashCode;
    }
}
