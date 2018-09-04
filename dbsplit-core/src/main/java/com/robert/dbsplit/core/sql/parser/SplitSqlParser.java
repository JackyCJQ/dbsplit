package com.robert.dbsplit.core.sql.parser;

/**
 * 分割的sql解析
 */
public interface SplitSqlParser {
    public static final SplitSqlParser INST = new SplitSqlParserDefImpl();

    public SplitSqlStructure parseSplitSql(String sql);
}
