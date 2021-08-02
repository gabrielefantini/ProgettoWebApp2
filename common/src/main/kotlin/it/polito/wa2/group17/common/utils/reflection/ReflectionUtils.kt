package it.polito.wa2.group17.common.utils.reflection

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

fun Field.isGenericType(): Boolean = type.typeParameters.isNotEmpty()

fun Method.hasReturnType(): Boolean = !returnType.equals(Void.TYPE) &&
        !returnType.equals(Unit::class.java) &&
        !returnType.equals(Void::class.java) &&
        !returnType.equals(Nothing::class.java)

@Suppress("UNCHECKED_CAST")
private fun <T> tryInstantiateClass(clazz: Class<T>, vararg args: Any?): T {
    if (args.size == 1) {
        if (args[0] is String) {
            val str = args[0] as String
            when (clazz.canonicalName) {
                Int::class.java.canonicalName -> return str.toInt() as T
                Short::class.java.canonicalName -> return str.toShort() as T
                Long::class.java.canonicalName -> return str.toLong() as T
                Double::class.java.canonicalName -> return str.toDouble() as T
                Float::class.java.canonicalName -> return str.toFloat() as T
                BigDecimal::class.java.canonicalName -> return str.toBigDecimal() as T
                BigInteger::class.java.canonicalName -> return str.toBigInteger() as T
                Byte::class.java.canonicalName -> return str.toByte() as T
                Boolean::class.java.canonicalName -> return str.toBoolean() as T
                CharArray::class.java.canonicalName -> return str.toCharArray() as T
                Regex::class.java.canonicalName -> return str.toRegex() as T
            }
            if (Enum::class.java.isAssignableFrom(clazz))
                return Enum::class.java.getDeclaredMethod("valueOf", Class::class.java, String::class.java)
                    .invoke(null, clazz, str) as T
        }

        if (clazz.canonicalName == String::class.java.canonicalName) {
            return if (Enum::class.java.isAssignableFrom(args[0]!!::class.java)) {
                (args[0] as Enum<*>).name as T
            } else args[0]!!.toString() as T
        }
    }

    val destinationConstructors = clazz.constructors

    val childConstructor = destinationConstructors.filter {
        it.parameterCount == args.size && it.parameterTypes.contentEquals(args.map { a -> a?.javaClass }.toTypedArray())
    }.map { it.trySetAccessible(); it }

    if (childConstructor.isNotEmpty()) return childConstructor[0].newInstance(*args) as T

    val emptyConstructor =
        destinationConstructors.filter { it.parameterCount == 0 }.map { it.trySetAccessible(); it }

    if (emptyConstructor.isNotEmpty()) return emptyConstructor[0].newInstance() as T

    throw NoSuchMethodException("Cannot instantiate ${clazz.canonicalName} from ${args.map { it.toString() }}")
}


fun <T> Class<T>.tryInstantiate(vararg args: Any?): T = tryInstantiateClass(this, *args)

fun <T : Any> KClass<T>.tryInstantiate(vararg args: Any?): T = tryInstantiateClass(this.java, *args)

fun <T : Iterable<Annotation>> T.containsAnnotation(annotationClass: KClass<out Annotation>): Boolean =
    any { annotationClass.java.isAssignableFrom(it::class.java) }

@Suppress("UNCHECKED_CAST")
fun <A : Annotation, T : Iterable<Annotation>> T.getAnnotation(annotationClass: KClass<A>): A? =
    find { annotationClass.java.isAssignableFrom(it::class.java) } as A?

fun KParameter.parameterType() = type.classifier as KClass<*>
fun <T> KProperty<T>.type() = getter.returnType.classifier as KClass<*>

fun <A : Annotation> KClass<A>.instantiateAnnotation(vararg args: Any): A = primaryConstructor!!.call(args)

fun <T : Any> Class<T>.distanceFrom(sourceClass: Class<*>): Int? {
    if (this == sourceClass) return 0
    var actualParent = sourceClass.superclass
    var i = 1
    while (actualParent != Any::class.java) {
        if (actualParent == this) return i
        i++
        actualParent = actualParent.superclass
    }
    return if (this == Any::class.java) i else null
}

fun <T : Any> KClass<T>.distanceFrom(sourceClass: KClass<*>): Int? =
    java.distanceFrom(sourceClass.java)

private fun searchForFieldsRecursive(clazz: Class<*>?, destination: MutableCollection<Field>) {
    if (clazz == null) return
    destination.addAll(clazz.declaredFields)
    searchForFieldsRecursive(clazz.superclass, destination)
}

fun <T> Class<T>.getAllFields(): Array<Field> {
    val destination = mutableListOf<Field>()
    searchForFieldsRecursive(this, destination)
    return destination.toTypedArray()
}

private fun searchForMethodsRecursive(clazz: Class<*>?, destination: MutableCollection<Method>) {
    if (clazz == null) return
    destination.addAll(clazz.declaredMethods)
    searchForMethodsRecursive(clazz.superclass, destination)
}
fun <T> Class<T>.getAllMethods(): Array<Method> {
    val destination = mutableListOf<Method>()
    searchForMethodsRecursive(this, destination)
    return destination.toTypedArray()
}
