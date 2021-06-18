package com.conght.common;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommonUtil {
    private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<>(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));
    private static final Set<Class<?>> GENERAL_TYPES = new HashSet<>(Arrays.asList(
            String.class, Date.class, Timestamp.class));

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static boolean isJavaDeclaredType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz) || GENERAL_TYPES.contains(clazz);
    }
}
