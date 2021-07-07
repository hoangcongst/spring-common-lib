package com.conght.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtil {
    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
    private static final Set<Class<?>> GENERAL_TYPES = new HashSet<>(Arrays.asList(
            String.class, Date.class, Timestamp.class, java.sql.Date.class));

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static boolean isJavaDeclaredType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz) || GENERAL_TYPES.contains(clazz);
    }

    public static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    static {
        WRAPPER_TYPE_MAP = new HashMap<Class<?>, Class<?>>(16);
        WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        WRAPPER_TYPE_MAP.put(Character.class, char.class);
        WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        WRAPPER_TYPE_MAP.put(Double.class, double.class);
        WRAPPER_TYPE_MAP.put(Float.class, float.class);
        WRAPPER_TYPE_MAP.put(Long.class, long.class);
        WRAPPER_TYPE_MAP.put(Short.class, short.class);
        WRAPPER_TYPE_MAP.put(Void.class, void.class);
    }
}
