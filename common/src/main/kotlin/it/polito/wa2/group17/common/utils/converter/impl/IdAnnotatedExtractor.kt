package it.polito.wa2.group17.common.utils.converter.impl

import it.polito.wa2.group17.common.utils.converter.CustomConverter
import it.polito.wa2.group17.common.utils.reflection.getAllFields
import org.springframework.data.annotation.Id
import java.lang.reflect.Field

class IdAnnotatedExtractor : CustomConverter<Any, Any> {
    override fun invoke(source: Any): Any {
        val ids = source.javaClass.getAllFields().filter { it.isAnnotationPresent(Id::class.java) }
        if (ids.size != 1)
            if (ids.isNotEmpty()) throw MultipleIdsError(ids)
            else throw IllegalArgumentException("Missing id annotated field for class ${source.javaClass.canonicalName}")
        return ids[0].apply { trySetAccessible() }.get(source)
    }
}

class MultipleIdsError(fields: List<Field>) :
    Error("Multiple ids found for class ${fields[0].declaringClass.canonicalName}: ${fields.map { it.name }}")
