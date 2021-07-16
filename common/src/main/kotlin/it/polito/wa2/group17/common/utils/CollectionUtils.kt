package it.polito.wa2.group17.common.utils

fun <T> Collection<T>.containsOnlyOnce(elem: T) = this.filter { it == elem }.size == 1
fun <T, R> Collection<T>.containsOnlyOnce(elem: R, mapper: (T) -> R) = this.map(mapper).filter { it == elem }.size == 1

inline fun <T, R : Comparable<R>> Iterable<T>.minByOr(or: T, selector: (T) -> R): T = minByOrNull(selector) ?: or
