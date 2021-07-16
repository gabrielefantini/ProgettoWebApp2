package it.polito.wa2.group17.common.utils

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

fun <A : Any, B : Any, I : Iterable<Pair<A, B>>> I.toMultiValueMap(): MultiValueMap<A, B> {
    val toRet = LinkedMultiValueMap<A, B>()
    forEach {
        toRet.add(it.first, it.second)
    }
    return toRet
}

fun <K : Comparable<K>, V> Map<out K, V>.toSortedMutableMap(): MutableMap<K, V> = TreeMap(this)

fun <K, V> Map<out K, V>.toSortedMutableMap(comparator: Comparator<in K>): MutableMap<K, V> =
    TreeMap<K, V>(comparator).apply { putAll(this@toSortedMutableMap) }
