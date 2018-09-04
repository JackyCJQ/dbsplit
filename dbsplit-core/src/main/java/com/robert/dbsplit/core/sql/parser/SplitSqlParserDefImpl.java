package com.robert.dbsplit.core.sql.parser;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;
import com.robert.dbsplit.core.sql.parser.SplitSqlStructure.SqlType;
import com.robert.dbsplit.excep.NotSupportedException;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

public class SplitSqlParserDefImpl implements SplitSqlParser {
    private static final Logger log = LoggerFactory.getLogger(SplitSqlParserDefImpl.class);
    //缓存的大小
    private static final int CACHE_SIZE = 1000;

    @SuppressWarnings("unchecked")
    //最近最少未使用类型
    private Map<String, SplitSqlStructure> cache = new LRUMap(CACHE_SIZE);

    public SplitSqlParserDefImpl() {
        log.info("Default SplitSqlParserDefImpl is used.");
    }

    //解析执行的sql
    public SplitSqlStructure parseSplitSql(String sql) {
        SplitSqlStructure splitSqlStructure = cache.get(sql);

        // Don't use if contains then get, race conditon may happens due to LRU
        // map
        if (splitSqlStructure != null)
            return splitSqlStructure;

        splitSqlStructure = new SplitSqlStructure();
        //数据库名字，数据表的名字
        String dbName = null;
        String tableName = null;
        //标识后面的字段是不是表的名字
        boolean inProcess = false;
        //标识是表的前面部分
        boolean previous = true;
        //标识是表的后面部分
        boolean sebsequent = false;


        StringBuffer sbPreviousPart = new StringBuffer();
        StringBuffer sbSebsequentPart = new StringBuffer();

        // Need to opertimize for better performance
        //利用druid来解析sql
        Lexer lexer = new Lexer(sql);
        do {
            lexer.nextToken();
            Token tok = lexer.token();
            //如果解析到头了
            if (tok == Token.EOF) {
                break;
            }

            if (tok.name != null)
                switch (tok.name) {
                    case "SELECT":
                        splitSqlStructure.setSqlType(SqlType.SELECT);
                        break;

                    case "INSERT":
                        splitSqlStructure.setSqlType(SqlType.INSERT);
                        break;

                    case "DELETE":
                        splitSqlStructure.setSqlType(SqlType.DELETE);
                        break;

                    case "UPDATE":
                        inProcess = true;
                        splitSqlStructure.setSqlType(SqlType.UPDATE);
                        break;

                    case "INTO":
                        //如果是插入类型into后面的就是表名
                        if (SqlType.INSERT.equals(splitSqlStructure.getSqlType()))
                            inProcess = true;
                        break;

                    case "FROM":
                        //插入或者是删除后面的是表名
                        if (SqlType.SELECT.equals(splitSqlStructure.getSqlType())
                                || SqlType.DELETE.equals(splitSqlStructure
                                .getSqlType()))
                            inProcess = true;
                        break;
                }

            if (sebsequent)
                sbSebsequentPart.append(
                        tok == Token.IDENTIFIER ? lexer.stringVal() : tok.name).append(" ");

            //处理表的名字
            if (inProcess) {
                if (dbName == null && tok == Token.IDENTIFIER) {
                    //获取到表的名字
                    dbName = lexer.stringVal();
                    //前半部分结束
                    previous = false;
                } else if (dbName != null && tableName == null && tok == Token.IDENTIFIER) {
                    //表的名字
                    tableName = lexer.stringVal();
                    //表名处理过程结束
                    inProcess = false;
                    //只有表的名字确定后面才是 表后半部分
                    sebsequent = true;
                }
            }
            if (previous)
                sbPreviousPart.append(tok == Token.IDENTIFIER ? lexer.stringVal() : tok.name).append(" ");

        } while (true);

        if (StringUtils.isEmpty(dbName) || StringUtils.isEmpty(tableName))
            throw new NotSupportedException("The split sql is not supported: " + sql);
        //设置数据库的名字和数据表的名字
        splitSqlStructure.setDbName(dbName);
        splitSqlStructure.setTableName(tableName);
        //根据表的名字分为了前后两个部分
        splitSqlStructure.setPreviousPart(sbPreviousPart.toString());
        splitSqlStructure.setSebsequentPart(sbSebsequentPart.toString());

        // if race condition, it is not severe
        //添加进缓存
        if (!cache.containsKey(splitSqlStructure))
            cache.put(sql, splitSqlStructure);
        return splitSqlStructure;
    }
}
