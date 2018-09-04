package com.robert.dbsplit.util.reflect;

import java.lang.reflect.Field;

/**
 * 反射处理字段
 */
public interface FieldHandler {
    public void handle(int index, Field field, Object value);
}
