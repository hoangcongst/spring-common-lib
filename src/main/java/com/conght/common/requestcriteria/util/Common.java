package com.conght.common.requestcriteria.util;

public class Common {
    public static <Y> Y getObjectFromTypeAndValueString(Class<Y> cls, String value) {
        if (float.class.equals(cls) || Float.class.equals(cls)) {
            return null;
        }
        return null;
    }

    public static String convertCamelToSnake(String str) {
        StringBuilder result = new StringBuilder();
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
