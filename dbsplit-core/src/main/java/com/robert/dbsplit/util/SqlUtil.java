package com.robert.dbsplit.util;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;
import com.robert.dbsplit.util.reflect.FieldHandler;
import com.robert.dbsplit.util.reflect.FieldVisitor;
import com.robert.dbsplit.util.reflect.ReflectionUtil;

public abstract class SqlUtil {
	public static class SqlRunningBean {
		private String sql;
		private Object[] params;

		public SqlRunningBean(String sql, Object[] params) {
			this.sql = sql;
			this.params = params;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public Object[] getParams() {
			return params;
		}

		public void setParams(Object[] params) {
			this.params = params;
		}
	}

	public static <T> SqlRunningBean generateInsertSql(T bean,
			String databasePrefix, String tablePrefix, int databseIndex,
			int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("insert into ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(bean.getClass()
					.getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databseIndex,
				tableIndex));

		sb.append("(");

		final List<Object> params = new LinkedList<Object>();

		new FieldVisitor<T>(bean).visit(new FieldHandler() {
			public void handle(int index, Field field, Object value) {
				if (index != 0)
					sb.append(",");

				sb.append(OrmUtil.javaFieldName2DbFieldName(field.getName()));

				if (value instanceof Enum)
					value = ((Enum<?>) value).ordinal();

				params.add(value);
			}
		});

		sb.append(") values (");
		sb.append(OrmUtil.generateParamPlaceholders(params.size()));
		sb.append(")");

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateInsertSql(T bean) {
		return generateInsertSql(bean, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateInsertSql(T bean,
			String databasePrefix) {
		return generateInsertSql(bean, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateInsertSql(T bean,
			String databasePrefix, String tablePrefix) {
		return generateInsertSql(bean, databasePrefix, tablePrefix, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean,
			String databasePrefix, String tablePrefix, int databaseIndex,
			int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("update ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(bean.getClass()
					.getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix,
				databaseIndex, tableIndex));

		sb.append("set ");

		final List<Object> params = new LinkedList<Object>();

		new FieldVisitor<T>(bean).visit(new FieldHandler() {
			public void handle(int index, Field field, Object value) {
				if (index != 0)
					sb.append(", ");

				sb.append(OrmUtil.javaFieldName2DbFieldName(field.getName()))
						.append("=? ");

				if (value instanceof Enum)
					value = ((Enum<?>) value).ordinal();

				params.add(value);
			}
		});

		sb.append("where ID = ?");

		params.add(ReflectionUtil.getFieldValue(bean, "id"));

		return new SqlRunningBean(sb.toString(), params.toArray());
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean) {
		return generateUpdateSql(bean, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean,
			String databasePrefix) {
		return generateUpdateSql(bean, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateUpdateSql(T bean,
			String databasePrefix, String tablePrefix) {
		return generateUpdateSql(bean, databasePrefix, tablePrefix, -1, -1);
	}
	
	
	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz, String databasePrefix, String tablePrefix, int databaseIndex,
			int tableIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append("delete from ");

		if (StringUtils.isEmpty(tablePrefix))
			tablePrefix = OrmUtil.javaClassName2DbTableName(clazz
					.getSimpleName());

		sb.append(getQualifiedTableName(databasePrefix, tablePrefix, databaseIndex,
				tableIndex));

		sb.append("where ID = ?");

		List<Object> params = new LinkedList<Object>();
		params.add(id);
		
		return new SqlRunningBean(sb.toString(), params.toArray());
	}
	
	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz) {
		return generateDeleteSql(id, clazz, null, null, -1, -1);
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz,
			String databasePrefix) {
		return generateDeleteSql(id, clazz, databasePrefix, null, -1, -1);
	}

	public static <T> SqlRunningBean generateDeleteSql(long id, Class<T> clazz,
			String databasePrefix, String tablePrefix) {
		return generateDeleteSql(id, clazz, databasePrefix, tablePrefix, -1, -1);
	}


	private static String getQualifiedTableName(String databasePrefix,
			String tablePrefix, int dbIndex, int tableIndex) {
		StringBuffer sb = new StringBuffer();

		if (!StringUtils.isEmpty(databasePrefix))
			sb.append(databasePrefix);

		if (dbIndex != -1)
			sb.append("_").append(dbIndex).append(".");

		if (!StringUtils.isEmpty(tablePrefix))
			sb.append(tablePrefix);

		if (tableIndex != -1)
			sb.append("_").append(tableIndex).append(" ");

		return sb.toString();
	}

	// TODO need to handle select, insert, delete, update separately

	// For SplitJdbcTemplate

	public static String[] getDbTableNamesSelect(String sql) {
		Lexer lexer = new Lexer(sql);

		String dbName = null;
		String tableName = null;
		boolean inProcess = false;

		for (;;) {
			lexer.nextToken();
			Token tok = lexer.token();
			if ("FROM".equals(tok.name))
				inProcess = true;
			else if ("WHERE".equals(tok.name))
				inProcess = false;
			if (inProcess) {
				if (dbName == null && (tok == Token.IDENTIFIER))
					dbName = lexer.stringVal();
				else if (dbName != null && (tok == Token.IDENTIFIER))
					tableName = lexer.stringVal();
			}
			if (tok == Token.EOF) {
				break;
			}
		}

		return new String[] { dbName, tableName };
	}

	public static String[] getDbTableNamesUpdate(String sql) {
		Lexer lexer = new Lexer(sql);

		String dbName = null;
		String tableName = null;
		boolean inProcess = false;

		for (;;) {
			lexer.nextToken();
			Token tok = lexer.token();
			if ("UPDATE".equals(tok.name))
				inProcess = true;
			else if ("SET".equals(tok.name))
				inProcess = false;
			if (inProcess) {
				if (dbName == null && (tok == Token.IDENTIFIER))
					dbName = lexer.stringVal();
				else if (dbName != null && (tok == Token.IDENTIFIER))
					tableName = lexer.stringVal();
			}
			if (tok == Token.EOF) {
				break;
			}
		}

		return new String[] { dbName, tableName };
	}

	public static String splitSelectSql(String sql, int dbNo, int tableNo) {
		Lexer lexer = new Lexer(sql);

		String dbName = null;
		String tableName = null;
		boolean inProcess = false;

		for (;;) {
			lexer.nextToken();
			Token tok = lexer.token();
			if ("FROM".equals(tok.name))
				inProcess = true;
			else if ("WHERE".equals(tok.name))
				inProcess = false;
			if (inProcess) {
				if (dbName == null && (tok == Token.IDENTIFIER))
					dbName = lexer.stringVal();
				else if (dbName != null && (tok == Token.IDENTIFIER))
					tableName = lexer.stringVal();
			}
			if (tok == Token.EOF) {
				break;
			}
		}

		sql = sql.replace(dbName, dbName + "_" + dbNo);
		sql = sql.replace(tableName, tableName + "_" + tableNo);

		return sql;
	}

	public static String splitUpdateSql(String sql, int dbNo, int tableNo) {
		Lexer lexer = new Lexer(sql);

		String dbName = null;
		String tableName = null;
		boolean inProcess = false;

		for (;;) {
			lexer.nextToken();
			Token tok = lexer.token();
			if ("UPDATE".equals(tok.name))
				inProcess = true;
			else if ("SET".equals(tok.name))
				inProcess = false;
			if (inProcess) {
				if (dbName == null && (tok == Token.IDENTIFIER))
					dbName = lexer.stringVal();
				else if (dbName != null && (tok == Token.IDENTIFIER))
					tableName = lexer.stringVal();
			}
			if (tok == Token.EOF) {
				break;
			}
		}

		sql = sql.replace(dbName, dbName + "_" + dbNo);
		sql = sql.replace(tableName, tableName + "_" + tableNo);

		return sql;
	}
}