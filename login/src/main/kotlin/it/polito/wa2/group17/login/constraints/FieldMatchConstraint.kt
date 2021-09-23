package it.polito.wa2.group17.login.constraints

import org.springframework.beans.BeanWrapperImpl
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass


@Constraint(validatedBy = [FieldsMatchConstraintValidator::class])
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Repeatable
annotation class FieldsMatch(
    val fieldA: String,
    val fieldB: String,
    val message: String = "Fields don't match!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {

    @Target(AnnotationTarget.TYPE)
    annotation class List(vararg val value: FieldsMatch)
}

class FieldsMatchConstraintValidator : ConstraintValidator<FieldsMatch, Any> {
    private lateinit var fieldA: String
    private lateinit var fieldB: String
    override fun initialize(constraintAnnotation: FieldsMatch) {
        fieldA = constraintAnnotation.fieldA
        fieldB = constraintAnnotation.fieldB
    }

    override fun isValid(value: Any, context: ConstraintValidatorContext): Boolean =
        BeanWrapperImpl(value).run { getPropertyValue(fieldA) == getPropertyValue(fieldB) }
}
