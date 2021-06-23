package com.conght.common.database.interceptor;

import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public class EntityUtil {
    public static boolean isRelationShip(Field field) {
        Stream<Annotation> annotations = Arrays.stream(field.getAnnotations());
        return !field.getType().isPrimitive() && annotations.anyMatch(annotation -> {
            Class<?> classAnnotation = annotation.annotationType();
            return classAnnotation.equals(JoinColumn.class) || classAnnotation.equals(JoinTable.class);
        });
    }
}
