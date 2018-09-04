package com.robert.dbsplit.core.sql.parser;

import org.springframework.util.StringUtils;

/**
 * 解析的sql
 */
public class SplitSqlStructure {
    //执行的四种类型
    public enum SqlType {
        SELECT, INSERT, UPDATE, DELETE
    }

    ;

    private SqlType sqlType;

    private String dbName;
    private String tableName;

    private String previousPart;
    private String sebsequentPart;

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPreviousPart() {
        return previousPart;
    }

    public void setPreviousPart(String previousPart) {
        this.previousPart = previousPart;
    }

    public String getSebsequentPart() {
        return sebsequentPart;
    }

    public void setSebsequentPart(String sebsequentPart) {
        this.sebsequentPart = sebsequentPart;
    }

    /**
     * 根据对应的序号 生成对应的sql
     *
     * @param dbNo
     * @param tableNo
     * @return
     */
    public String getSplitSql(int dbNo, int tableNo) {
        if (sqlType == null || StringUtils.isEmpty(dbName)
                || StringUtils.isEmpty(tableName)
                || StringUtils.isEmpty(previousPart)
                || StringUtils.isEmpty(sebsequentPart))
            throw new IllegalStateException(
                    "The split SQL should be constructed after the SQL is parsed completely.");
        //拼接分表后的sql语句
        StringBuffer sb = new StringBuffer();
        //dbName_1.tableName_1
        sb.append(previousPart).append(" ");
        sb.append(dbName).append("_").append(dbNo);
        sb.append(".");
        sb.append(tableName).append("_").append(tableNo).append(" ");
        sb.append(sebsequentPart);
        return sb.toString();
    }
}
