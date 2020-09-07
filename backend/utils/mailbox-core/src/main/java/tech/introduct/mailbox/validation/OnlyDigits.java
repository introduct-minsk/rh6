package tech.introduct.mailbox.validation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = OnlyDigits.Validator.class)
public @interface OnlyDigits {

    String message() default "value must contain only digit characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<OnlyDigits, String> {

        @Override
        public boolean isValid(String number, ConstraintValidatorContext context) {
            return isEmpty(number) || isDigits(number);
        }
    }
}
