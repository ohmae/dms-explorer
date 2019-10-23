package net.mm2d.dmsexplorer.domain.entity

import java.text.Collator
import java.util.*
import kotlin.Comparator

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
private val collator = Collator.getInstance(Locale.ENGLISH)

private fun <T> ascendingTextComparator(selector: (o: T) -> String): Comparator<T> =
    Comparator { o1: T, o2: T -> collator.compare(selector.invoke(o1), selector.invoke(o2)) }

private fun <T> descendingTextComparator(selector: (o: T) -> String): Comparator<T> =
    Comparator { o1: T, o2: T -> collator.compare(selector.invoke(o2), selector.invoke(o1)) }

fun <T> Iterable<T>.sortedByText(selector: (o: T) -> String): List<T> =
    sortedWith(ascendingTextComparator(selector))

fun <T> Iterable<T>.sortedByTextDescending(selector: (o: T) -> String): List<T> =
    sortedWith(descendingTextComparator(selector))

fun <T> Iterable<T>.sortedByText(ascending: Boolean, selector: (o: T) -> String): List<T> =
    if (ascending) sortedByText(selector) else sortedByTextDescending(selector)

fun <T, R : Comparable<R>> Iterable<T>.sortedBy(ascending: Boolean, selector: (o: T) -> R): List<T> =
    if (ascending) sortedBy(selector) else sortedByDescending(selector)
