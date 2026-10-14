package com.grovesoft.boctracker.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Set<String> allowed;
    private boolean nullable;
    private String delimiter;

    @Override
    public void initialize(EnumValue annotation) {
        this.allowed = Set.of(annotation.allowed());
        this.nullable = annotation.nullable();
        this.delimiter = annotation.delimiter();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return nullable;
        }
        if (!delimiter.isEmpty()) {
            return Arrays.stream(value.split(java.util.regex.Pattern.quote(delimiter), -1))
                    .allMatch(allowed::contains);
        }
        if (allowed.contains(value)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "허용되지 않은 값입니다. 허용값: " + allowed.stream().sorted().collect(Collectors.joining(", ")))
                .addConstraintViolation();
        return false;
    }
}
