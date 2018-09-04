package com.robert.dbsplit.util.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * 反射工具类
 */
public abstract class ReflectionUtil {
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

    /**
     * 获取可访问的字段
     *
     * @param clazz
     * @return
     */
    public static List<Field> getClassEffectiveFields(Class<? extends Object> clazz) {
        List<Field> effectiveFields = new LinkedList<Field>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //如果字段是私有的
                if (!field.isAccessible()) {
                    try {
                        //看是否有get方法
                        Method method = clazz.getMethod(fieldName2GetterName(field.getName()));

                        if (method.getReturnType() != field.getType()) {
                            log.error(
                                    "The getter for field {} may not be correct.", field);
                            continue;
                        }
                    } catch (NoSuchMethodException e) {
                        log.error(
                                "Fail to obtain getter method for non-accessible field {}.",
                                field);
                        log.error("Exception--->", e);

                        continue;
                    } catch (SecurityException e) {
                        log.error(
                                "Fail to obtain getter method for non-accessible field {}.",
                                field);
                        log.error("Exception--->", e);

                        continue;
                    }

                }
                effectiveFields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return effectiveFields;
    }

    /**
     * 生成get方法的形式
     *
     * @param fieldName
     * @return
     */
    public static String fieldName2GetterName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }

    /**
     * 生成set方法的形式
     *
     * @param fieldName
     * @return
     */
    public static String fieldName2SetterName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    /**
     * 获取某个对象字段的值
     *
     * @param bean
     * @param fieldName
     * @param <T>
     * @return
     */
    public static <T> Object getFieldValue(T bean, String fieldName) {
        Field field = null;
        try {
            field = bean.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            log.error("Fail to obtain field {} from bean {}.", fieldName, bean);
            log.error("Exception--->", e);
            throw new IllegalStateException("Refelction error: ", e);
        } catch (SecurityException e) {
            log.error("Fail to obtain field {} from bean {}.", fieldName, bean);
            log.error("Exception--->", e);
            throw new IllegalStateException("Refelction error: ", e);
        }
        //暴力反射
        boolean access = field.isAccessible();
        field.setAccessible(true);

        Object result = null;
        try {
            result = field.get(bean);
        } catch (IllegalArgumentException e) {
            log.error("Fail to obtain field {}'s value from bean {}.",
                    fieldName, bean);
            log.error("Exception--->", e);
            throw new IllegalStateException("Refelction error: ", e);
        } catch (IllegalAccessException e) {
            log.error("Fail to obtain field {}'s value from bean {}.",
                    fieldName, bean);
            log.error("Exception--->", e);
            throw new IllegalStateException("Refelction error: ", e);
        }
        //最后在设置回来
        field.setAccessible(access);
        return result;
    }

    /**
     * 如果set方法中含有枚举类
     *
     * @param clazz
     * @param methodName
     * @return
     */
    public static Method searchEnumSetter(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (method.getParameterCount() > 0) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (Enum.class.isAssignableFrom(paramType))
                        return method;
                }
            }
        }

        return null;
    }
}
