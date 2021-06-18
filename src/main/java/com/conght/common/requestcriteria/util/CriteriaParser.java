package com.conght.common.requestcriteria.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://www.baeldung.com/rest-api-query-search-language-more-operations
 */

public class CriteriaParser {

    private static final Map<String, Operator> ops;

    private static final Pattern SpecCriteriaRegex = Pattern.compile("^(\\w+?)(" + String.join("|", SearchOperation.SIMPLE_OPERATION_SET)
            + ")(\\p{Punct}?)(\\w+?)(\\p{Punct}?)$");

    private static final Pattern SimpleCriteriaRegex = Pattern.compile("(" + String.join("|", SearchOperation.SIMPLE_OPERATION_SET)
            + ")?(\\p{IsHangul}+|\\w+)");

    private enum Operator {
        OR(1), AND(2);
        final int precedence;

        Operator(int p) {
            precedence = p;
        }
    }

    static {
        ops = Map.of("AND", Operator.AND, "OR", Operator.OR, "or", Operator.OR, "and", Operator.AND);
    }

    private static boolean isHigherPrecedenceOperator(String currOp, String prevOp) {
        return (ops.containsKey(prevOp) && ops.get(prevOp).precedence >= ops.get(currOp).precedence);
    }

    public Deque<?> parse(String searchParam) {

        Deque<Object> output = new LinkedList<>();
        Deque<String> stack = new LinkedList<>();

        Arrays.stream(searchParam.split("\\s+")).forEach(token -> {
            if (ops.containsKey(token)) {
                while (!stack.isEmpty() && isHigherPrecedenceOperator(token, stack.peek()))
                    output.push(stack.pop()
                            .equalsIgnoreCase(SearchOperation.OR_OPERATOR) ? SearchOperation.OR_OPERATOR : SearchOperation.AND_OPERATOR);
                stack.push(token.equalsIgnoreCase(SearchOperation.OR_OPERATOR) ? SearchOperation.OR_OPERATOR : SearchOperation.AND_OPERATOR);
            } else if (token.equals(SearchOperation.LEFT_PARANTHESIS)) {
                stack.push(SearchOperation.LEFT_PARANTHESIS);
            } else if (token.equals(SearchOperation.RIGHT_PARANTHESIS)) {
                while (!stack.peek()
                        .equals(SearchOperation.LEFT_PARANTHESIS))
                    output.push(stack.pop());
                stack.pop();
            } else {

                Matcher matcher = SpecCriteriaRegex.matcher(token);
                while (matcher.find()) {
                    output.push(new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5)));
                }
            }
        });

        while (!stack.isEmpty())
            output.push(stack.pop());

        return output;
    }

    /**
     * parse value for request object to push into criteria
     * @param reqObject
     * @return
     */
    public List<SpecSearchCriteria> parse(Object reqObject) {
        List<SpecSearchCriteria> output = new LinkedList<>();
        try {
            Map<String, Object> properties = this.showFields(reqObject);
            properties.forEach((k, v) -> {
                if(v != null && !k.equals("page") && !k.equals("size") && !k.equals("class")) {
                    if(v instanceof Number)
                        output.add(new SpecSearchCriteria(k, SearchOperation.EQUALITY, v));
                    else {
                        Matcher matcher = SimpleCriteriaRegex.matcher((CharSequence) v);
                        while (matcher.find()) {
                            output.add(new SpecSearchCriteria(k, matcher.group(1) != null ?
                                    SearchOperation.getSimpleOperation(matcher.group(1).charAt(0)) : SearchOperation.EQUALITY,
                                    matcher.group(2)));
                        }
                    }
                }  
            });
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return output;
    }

    public Map<String, Object> showFields(Object o) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = o.getClass();
        Map<String, Object> properties = new HashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
            String propertyName = propertyDesc.getName();
            Object value = propertyDesc.getReadMethod().invoke(o);
            properties.put(propertyName, value);
        }
        return properties;
    }
}
