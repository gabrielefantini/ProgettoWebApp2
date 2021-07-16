package it.polito.wa2.group17.common.utils.converter

import it.polito.wa2.group17.common.utils.reflection.*
import it.polito.wa2.group17.common.utils.toMultiValueMap
import it.polito.wa2.group17.common.utils.toSortedMutableMap
import org.openjdk.jol.vm.VM
import org.springframework.util.MultiValueMap
import java.lang.reflect.Field
import java.util.stream.IntStream.range
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible


@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ConvertibleAlias(val value: From, vararg val others: From) {
    annotation class From(val source: KClass<*> = Any::class, val field: String)

}

fun ConvertibleAlias.getAllMappings(): Array<ConvertibleAlias.From> = arrayOf(this.value, *this.others)

/**
 * Enable auto conversion on compatible generic classes.
 * BEWARE, USE IT ONLY IF THE GENERIC TYPES ARE COMPATIBLE!!!
 * BECAUSE OF THE JVM TYPE ERASURE THE ASSIGNMENT IN CONVERSION WILL NOT COMPLAIN
 * BUT WHEN YOU WILL WANT TO ACCESS THE GENERIC TYPE ASSIGNING IT, IT WILL BRAKE.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class SafeGenericConversion

/**
 * Allow auto conversion of generic collections.
 * The value should be the type of the target collection.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ConvertibleCollection(
    val value: KClass<*>,
    val valueCustomConversion: CustomConversion = CustomConversion(CustomConversion.Using(IdentityCustomConverter::class)),
    val concreteCollectionType: KClass<out MutableCollection<*>> = ArrayList::class,
)

/**
 * Allow auto conversion of generic Maps.
 * the fields describes the map types.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
annotation class ConvertibleMap(
    val keyType: KClass<*>,
    val valueType: KClass<*>,
    val kyeCustomConversion: CustomConversion = CustomConversion(CustomConversion.Using(IdentityCustomConverter::class)),
    val valueCustomConversion: CustomConversion = CustomConversion(CustomConversion.Using(IdentityCustomConverter::class)),
    val concreteMapType: KClass<out MutableMap<*, *>> = HashMap::class
)


class InconvertibleGenericClassException(message: String) : Exception(message)

@Suppress("UNCHECKED_CAST")
object ReflectionConverter {

    private val BASIC_TYPES = arrayOf(
        String::class,
        Long::class,
        Int::class,
        Short::class,
        Double::class,
        Float::class,
        Byte::class,
        Boolean::class
    )

    private fun <S : Any, D : Any> extractAndMapFields(
        sourceClass: KClass<out S>,
        destinationClass: KClass<out D>
    ): Pair<Map<String, Field>, MultiValueMap<String, Field>> {
        val sourceFields = listOf(*sourceClass.java.getAllFields()).associateBy { it.trySetAccessible(); it.name }
        val destinationFields =
            listOf(*destinationClass.java.getAllFields()).map {
                it.trySetAccessible()
                if (it.isAnnotationPresent(ConvertibleAlias::class.java)) {
                    val fromAnn = it.getAnnotation(ConvertibleAlias::class.java).getAllMappings().filter { c ->
                        c.source.java.isAssignableFrom(sourceClass.java)
                    }.sortedBy { c -> c.source.distanceFrom(sourceClass) }.map { c -> c.field to it }
                    if (fromAnn.isNotEmpty()) fromAnn.first() else it.name to it
                } else it.name to it
            }.toMultiValueMap()
        return sourceFields to destinationFields
    }

    private fun <S : Any, D : Any> extractAndMapParameters(
        sourceClass: KClass<out S>,
        destinationClass: KClass<out D>
    ): Pair<Map<String, Field>, MultiValueMap<String, KParameter>> {
        val sourceFields = sourceClass.java.getAllFields().associateBy { it.isAccessible = true; it.name }

        val destinationParameters =
            destinationClass.primaryConstructor!!.parameters.map {
                if (it.annotations.containsAnnotation(ConvertibleAlias::class)) {
                    val fromAnn = (it.annotations.getAnnotation(ConvertibleAlias::class)!!)
                        .getAllMappings().filter { c ->
                            c.source.java.isAssignableFrom(sourceClass.java)
                        }.sortedBy { c -> c.source.distanceFrom(sourceClass) }
                    if (fromAnn.isNotEmpty()) fromAnn.first().field to it else it.name!! to it
                } else it.name!! to it
            }.toMultiValueMap()
        return sourceFields to destinationParameters
    }

    private fun raiseInconvertibleGeneric(
        sourceName: String,
        destinationName: String,
        underlyingClass: Class<*>
    ): Nothing = throw InconvertibleGenericClassException(
        "Couldn't automatically convert $sourceName " +
                "to $destinationName " +
                "because is a generic object but it is not possible to instantiate" +
                "the underlying class ${underlyingClass.canonicalName}."
    )

    private fun convertFromCollection(
        sourceField: Field,
        sourceValue: Collection<Any?>,
        sourceClass: KClass<*>,
        destinationName: String,
        convertibleCollection: ConvertibleCollection,
        alreadyInstantiated: MutableMap<Long, Any>,
    ): MutableCollection<*> {
        try {
            val destinationCollectionType = convertibleCollection.value
            val factory = convertibleCollection.valueCustomConversion
            val destinationCollection =
                convertibleCollection.concreteCollectionType.tryInstantiate(sourceValue) as MutableCollection<Any?>
            sourceValue.forEach { v ->
                val destinationElement = v?.let {
                    doConvert(v, destinationCollectionType, factory.applicableFrom(sourceClass), alreadyInstantiated)
                }
                destinationCollection.add(destinationElement)
            }
            return destinationCollection
        } catch (e: NoSuchMethodException) {
            raiseInconvertibleGeneric(
                "${sourceValue.javaClass.canonicalName}.${sourceField.name}",
                destinationName,
                sourceValue.javaClass
            )
        }
    }

    private fun convertFromMap(
        sourceField: Field,
        sourceValue: Map<Any?, Any?>,
        sourceClass: KClass<*>,
        destinationName: String,
        convertibleMap: ConvertibleMap,
        alreadyInstantiated: MutableMap<Long, Any>
    ): MutableMap<*, *> {
        try {
            val keyType = convertibleMap.keyType
            val valueType = convertibleMap.valueType
            val keyFactory = convertibleMap.kyeCustomConversion
            val valueFactory = convertibleMap.valueCustomConversion
            val destinationMap =
                convertibleMap.concreteMapType.tryInstantiate(sourceValue) as MutableMap<Any?, Any?>
            sourceValue.forEach { (k, v) ->
                val destinationKey = k?.let {
                    doConvert(k, keyType, keyFactory.applicableFrom(sourceClass), alreadyInstantiated)
                }
                val destinationValue = v?.let {
                    doConvert(v, valueType, valueFactory.applicableFrom(sourceClass), alreadyInstantiated)
                }
                destinationMap[destinationKey] = destinationValue
            }
            return destinationMap
        } catch (e: NoSuchMethodException) {
            raiseInconvertibleGeneric(
                "${sourceValue.javaClass.canonicalName}.${sourceField.name}",
                destinationName,
                sourceValue.javaClass
            )
        }
    }

    private fun <D : Any> doConvertGenericClass(
        source: Any,
        destination: D,
        destinationClass: KClass<out D>,
        alreadyInstantiated: MutableMap<Long, Any>
    ) {
        if (BASIC_TYPES.contains(source::class)) return

        val instantiatedFields: Map<Field, Any?> = doConvert(
            source,
            destinationClass,
            alreadyInstantiated,
            this::extractAndMapFields,
            { field -> field.type.kotlin },
            { f, a -> f.isAnnotationPresent(a.java) },
            { f, a -> f.getAnnotation(a.java) },
            { f -> f.name }
        )
        instantiatedFields.forEach { (f, v) -> f.set(destination, v) }

    }

    private fun <D : Any> doConvertDataClass(
        source: Any,
        destinationClass: KClass<out D>,
        alreadyInstantiated: MutableMap<Long, Any>
    ): D {

        if (destinationClass.primaryConstructor!!.parameters.size == 1 &&
            destinationClass.primaryConstructor!!.parameters[0].parameterType() == source::class
        ) return destinationClass.primaryConstructor!!.call(source)
        if (BASIC_TYPES.contains(source::class))
            throw IllegalArgumentException(
                "Trying to instantiate a complex data object ${destinationClass.java.canonicalName} " +
                        "from a basic type ${source::class.java.canonicalName}!"
            )
        val instantiatedParameters: Map<KParameter, Any?> = doConvert(
            source,
            destinationClass,
            alreadyInstantiated,
            this::extractAndMapParameters,
            KParameter::parameterType,
            { kParameter, kClass -> kParameter.annotations.containsAnnotation(kClass) },
            { kParameter, kClass -> kParameter.annotations.getAnnotation(kClass) },
            KParameter::name
        )
        val sortedParameters =
            instantiatedParameters.mapKeys { e -> e.key.index }.toSortedMutableMap()

        if (destinationClass.primaryConstructor!!.parameters.size != sortedParameters.size) {
            for (i in range(0, destinationClass.primaryConstructor!!.parameters.size))
                if (!sortedParameters.containsKey(i))
                    if (destinationClass.primaryConstructor!!.parameters[i].type.isMarkedNullable)
                        sortedParameters[i] = null
                    else throw IllegalArgumentException(
                        "Couldn't instantiate data class ${destinationClass.java.canonicalName} " +
                                "because parameter ${destinationClass.primaryConstructor!!.parameters[i].name} is not nullable but source value is null"
                    )
        }

        return destinationClass.primaryConstructor!!.apply { isAccessible = true }
            .call(*sortedParameters.values.toTypedArray())
    }

    private fun <DF, D : Any> doConvert(
        source: Any,
        destinationClass: KClass<out D>,
        alreadyInstantiated: MutableMap<Long, Any>,
        sourceAndDestinationFieldProvider: (KClass<*>, KClass<out D>) -> Pair<Map<String, Field>, MultiValueMap<String, DF>>,
        destinationFieldTypeExtractor: (DF) -> KClass<*>,
        destinationFieldAnnotationChecker: (DF, KClass<out Annotation>) -> Boolean,
        destinationFieldAnnotationExtractor: (DF, KClass<out Annotation>) -> Annotation?,
        destinationFieldNameExtractor: (DF) -> String?
    ): Map<DF, Any?> {
        val sourceClass = source::class
        val (sourceFields, destinationParameters) = sourceAndDestinationFieldProvider(sourceClass, destinationClass)
        val instantiatedParameters: MutableMap<DF, Any?> = mutableMapOf()
        sourceFields.forEach {
            val sourceField = it.value
            val sourceValue = sourceField.get(source) ?: return@forEach
            val namedDestinationParams = destinationParameters[it.key]
            namedDestinationParams?.let { nnd ->
                nnd.forEach { destinationParameter ->
                    destinationParameter?.let {
                        if (destinationFieldAnnotationChecker(
                                destinationParameter, ConvertibleCollection::class
                            ) && Collection::class.java.isAssignableFrom(sourceValue.javaClass)
                        ) {
                            val collection = convertFromCollection(
                                sourceField,
                                sourceValue as Collection<Any?>,
                                sourceClass,
                                "${destinationClass.java.canonicalName}." +
                                        destinationFieldNameExtractor(destinationParameter),
                                destinationFieldAnnotationExtractor(
                                    destinationParameter, ConvertibleCollection::class
                                ) as ConvertibleCollection,
                                alreadyInstantiated
                            )
                            instantiatedParameters[destinationParameter] = collection
                        } else if (destinationFieldAnnotationChecker(
                                destinationParameter, ConvertibleMap::class
                            ) && Map::class.java.isAssignableFrom(sourceValue.javaClass)
                        ) {
                            val map = convertFromMap(
                                sourceField,
                                sourceValue as Map<Any?, Any?>,
                                sourceClass,
                                destinationClass.java.canonicalName +
                                        ".${destinationFieldNameExtractor(destinationParameter)}",
                                destinationFieldAnnotationExtractor(
                                    destinationParameter,
                                    ConvertibleMap::class
                                ) as ConvertibleMap,
                                alreadyInstantiated
                            )
                            instantiatedParameters[destinationParameter] = map
                        } else if (destinationFieldTypeExtractor(destinationParameter).java != sourceField.type &&
                            !(destinationFieldTypeExtractor(destinationParameter)
                                .java.isAssignableFrom(sourceField.type))
                        ) {

                            val destinationFieldInstance = doConvert(
                                sourceValue,
                                destinationFieldTypeExtractor(destinationParameter),
                                (destinationFieldAnnotationExtractor(
                                    destinationParameter, CustomConversion::class
                                ) as? CustomConversion)?.applicableFrom(sourceClass),
                                alreadyInstantiated
                            )
                            instantiatedParameters[destinationParameter] = destinationFieldInstance
                        } else if (sourceField.isGenericType() &&
                            !destinationFieldAnnotationChecker(destinationParameter, SafeGenericConversion::class)
                        ) {
                            throw InconvertibleGenericClassException(
                                "Couldn't automatically convert ${sourceClass.simpleName}.${sourceField.name} " +
                                        "to ${destinationClass.simpleName}." +
                                        "${destinationFieldNameExtractor(destinationParameter)} " +
                                        "because is generic. " +
                                        "In order to enable auto conversion it should be either annotated as " +
                                        "@SafeGeneric or being a collection annotated as @ConvertibleCollection " +
                                        "or being a map annotated as @ConvertibleMap"
                            )
                        } else instantiatedParameters[destinationParameter] = sourceValue

                    }
                }
            }
        }
        return instantiatedParameters
    }

    private fun <D : Any> doConvert(
        source: Any,
        destinationClass: KClass<out D>,
        customConversion: CustomConversion.Using? = null,
        alreadyInstantiated: MutableMap<Long, Any>
    ): D {
        val sourceClass = source::class
        if (BASIC_TYPES.contains(destinationClass) && sourceClass == destinationClass) return source as D

        val instance = alreadyInstantiated[VM.current().addressOf(source)]
        return if (instance != null && instance::class == destinationClass) {
            instance as D
        } else {
            try {
                if (customConversion != null && customConversion.converter != IdentityCustomConverter::class) {
                    val converter = customConversion.createConverter<Any, D>()
                    val destination = converter(source)
                    alreadyInstantiated[VM.current().addressOf(source)] = destination
                    destination
                } else if (destinationClass.isData) {
                    val destination = doConvertDataClass(source, destinationClass, alreadyInstantiated)
                    alreadyInstantiated[VM.current().addressOf(source)] = destination
                    destination
                } else {
                    val destination = destinationClass.tryInstantiate(source)
                    alreadyInstantiated[VM.current().addressOf(source)] = destination
                    doConvertGenericClass(source, destination, destinationClass, alreadyInstantiated)
                    destination
                }

            } catch (e: NoSuchMethodException) {
                throw RuntimeException(
                    "Couldn't instantiate '${destinationClass.java.canonicalName}' " +
                            "because has no compatible constructor! " +
                            "Provide this class with empty constructor or with one taking as single argument a ${sourceClass.java.canonicalName}. " +
                            "Alternatively, try annotate the corresponding field with @CustomConversion.", e
                )
            }
        }
    }


    fun <D : Any> convert(
        source: Any,
        destination: D,
        destinationClass: KClass<out D>
    ) {
        doConvertGenericClass(source, destination, destinationClass, mutableMapOf())
    }

    fun <D : Any> convert(
        source: Any,
        destinationClass: KClass<out D>,
        customConversion: CustomConversion.Using? = null
    ): D = doConvert(source, destinationClass, customConversion, mutableMapOf())


    inline fun <reified D : Any> convert(source: Any, customConversion: CustomConversion.Using? = null): D =
        convert(source, D::class, customConversion)


    inline fun <reified D : Any> convert(source: Any, destination: D) =
        convert(source, destination, D::class)

}

inline fun <reified D : Any> Any.convertTo(): D = ReflectionConverter.convert(this)
inline fun <reified D : Any> Any.convertTo(destination: D) =
    ReflectionConverter.convert(this, destination)
