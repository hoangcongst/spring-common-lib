package com.conght.common.validator.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "^-?\\d*(\\.\\d+)?$|^:(-?\\d*(\\.\\d+)?)\\|(-?\\d*(\\.\\d+)?)$")
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {}
)
public @interface ValidNumberOrNumberRange {
    String message() default "Invalid Number Format!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}