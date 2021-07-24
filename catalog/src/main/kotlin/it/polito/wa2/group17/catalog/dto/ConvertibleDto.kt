package it.polito.wa2.group17.catalog.dto

import it.polito.wa2.group17.common.utils.converter.convertTo

interface ConvertibleDto<S> {
    companion object Factory {
        inline fun <reified T : ConvertibleDto<S>, reified S : Any> fromEntity(source: S): T {
            return source.convertTo()
        }
    }
}


inline fun <reified S : Any, reified T : ConvertibleDto<S>> T.fromEntity(source: S): T {
    source.convertTo(this)
    return this
}
