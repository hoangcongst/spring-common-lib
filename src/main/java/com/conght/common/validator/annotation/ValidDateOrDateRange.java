package com.conght.common.validator.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import java.lang.annotation.*;

@Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$|^:(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\|(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})$")
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {}
)
public @interface ValidDateOrDateRange {
    String message() default "Invalid Date Format, the right format is yyyy-MM-dd HH:mm:ss";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}