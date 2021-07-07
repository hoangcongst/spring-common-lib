package com.conght.common.requestcriteria;

import lombok.SneakyThrows;
import com.conght.common.requestcriteria.util.Common;
import com.conght.common.requestcriteria.util.SpecSearchCriteria;
import org.hibernate.query.criteria.internal.ValueHandlerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseSpecification<T> implements Specification<T> {
    private final SpecSearchCriteria criteria;

    public BaseSpecification(final SpecSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    public SpecSearchCriteria getCriteria() {
        return criteria;
    }

    @SneakyThrows
    @Override
    public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder builder) {
        Path key = getPath(root);
        switch (criteria.getOperation()) {
            case EQUALITY:
                if (criteria.getValue() instanceof String && typeOfDateTime(key.getJavaType())) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return builder.equal(key, simpleDateFormat.parse(criteria.getValue().toString()));
                }
                return builder.equal(key, criteria.getValue());
            case NEGATION:
                return builder.notEqual(key, criteria.getValue());
            case GREATER_THAN:
                return builder.greaterThan(key, criteria.getValue().toString());
            case LESS_THAN:
                return builder.lessThan(key, criteria.getValue().toString());
            case RANGE:
                String[] query = criteria.getValue().toString().split("\\|");
                if (ValueHandlerFactory.isNumeric(key.getJavaType())) {
                    return builder.between(getPath(root), Double.parseDouble(query[0]),
                            Double.parseDouble(query[1]));
                } else if (typeOfDateTime(key.getJavaType())) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return builder.between(getPath(root), simpleDateFormat.parse(query[0]),
                            simpleDateFormat.parse(query[1]));
                }
            case IN:
                List<?> queryList = addSpecificTypeParameterForList(key.getJavaType(), criteria.getValue());
                if (criteria.getKey().endsWith("Id"))
                    return root.get(criteria.getKey().substring(0, criteria.getKey().length() - 2)).in(queryList);
                return root.get(criteria.getKey()).in(queryList);
            case LIKE:
                return builder.like(key, criteria.getValue().toString());
            case STARTS_WITH:
                return builder.like(key, criteria.getValue() + "%");
            case ENDS_WITH:
                return builder.like(key, "%" + criteria.getValue());
            case CONTAINS:
                return builder.like(key, "%" + criteria.getValue() + "%");
            default:
                return null;
        }
    }

    private boolean typeOfDateTime(Class<?> type) {
        return type.equals(java.util.Date.class) || type.equals(Timestamp.class)
                || type.equals(java.sql.Date.class);
    }

    List<T> addSpecificTypeParameterForList(Class<T> cls, Object data) {
        return (List<T>) data;
    }

    /**
     * Finding path for attributes in model, firstly, this function will try to find with raw name from criteria,
     * If no attributes found, it will throw exception, then in Catchblock, it will check type of attribute, if it's relationship,
     * root node will join to table in relationship, then return the node.
     * @param root
     * @return
     * @throws Exception
     */
    private Path<T> getPath(Root<T> root) throws Exception {
        try {
            return root.get(criteria.getKey());
        } catch (Exception e) {
            Set<SingularAttribute<? super T, ?>> attributes = root.getModel().getSingularAttributes();
            for (SingularAttribute<? super T, ?> attr : attributes) {
                if (!attr.getPersistentAttributeType().equals(Attribute.PersistentAttributeType.BASIC)
                        && criteria.getKey().startsWith(attr.getName())) {
                    return root.join(attr.getName()).get(Common.convertCamelToSnake(
                            criteria.getKey().substring(attr.getName().length())));
                }
            }
        }
        throw new Exception(String.format("Param %s not allow", criteria.getKey()));
    }
}
