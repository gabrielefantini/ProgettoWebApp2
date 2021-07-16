package it.polito.wa2.group17.common.utils.converter

import it.polito.wa2.group17.common.utils.reflection.distanceFrom
import kotlin.reflect.KClass


interface CustomConverter<S, D> : (S) -> D {
    override fun invoke(source: S): D
}


class IdentityCustomConverter : CustomConverter<Any?, Any?> {
    override fun invoke(source: Any?): Any? = source

}

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
annotation class CustomConversion(val value: Using, vararg val others: Using) {
    annotation class Using(
        val converter: KClass<out CustomConverter<*, *>>,
        val valuesFrom: KClass<*> = Any::class
    )
}

fun CustomConversion.getAll(): Array<CustomConversion.Using> = arrayOf(value, *others)


@Suppress("UNCHECKED_CAST")
fun <S, D> CustomConversion.Using.createConverter(): CustomConverter<S, D> =
    converter.java.getConstructor().newInstance() as CustomConverter<S, D>

fun CustomConversion.applicableFrom(sourceClass: KClass<*>): CustomConversion.Using? =
    getAll().filter { it.valuesFrom.java.isAssignableFrom(sourceClass.java) }
        .minByOrNull { it.valuesFrom.distanceFrom(sourceClass)!! } //find first in inheritance hierarchy
