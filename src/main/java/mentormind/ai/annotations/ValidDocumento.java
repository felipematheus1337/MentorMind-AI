package mentormind.ai.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import mentormind.ai.validator.DocumentoValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DocumentoValidator.class)
public @interface ValidDocumento {
    String message() default "Documento inválido: aceito apenas .md, .txt ou pdf";
    Class<?>[] groups() default {};
     Class<? extends Payload>[] payload() default {};
}
