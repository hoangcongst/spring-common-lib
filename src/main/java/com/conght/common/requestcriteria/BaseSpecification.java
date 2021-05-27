package com.conght.common.requestcriteria;

import com.conght.common.requestcriteria.util.Common;
import com.conght.common.requestcriteria.util.SpecSearchCriteria;
import lombok.SneakyThrows;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Set;

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
                return builder.equal(key, criteria.getValue());
            case NEGATION:
                return builder.notEqual(key, criteria.getValue());
            case GREATER_THAN:
                return builder.greaterThan(key, criteria.getValue().toString());
            case LESS_THAN:
                return builder.lessThan(key, criteria.getValue().toString());
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
