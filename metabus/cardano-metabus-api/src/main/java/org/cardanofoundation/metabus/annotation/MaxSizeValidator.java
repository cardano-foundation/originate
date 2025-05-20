package org.cardanofoundation.metabus.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MaxSizeValidator  implements ConstraintValidator<MaxByteSize, CharSequence> {

    private int maxSize;


    @Override
    public void initialize(MaxByteSize constraintAnnotation) {
        maxSize = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true;
        }

        byte[] bytes = value.toString().getBytes();
        return bytes.length <= maxSize;

    }
}
