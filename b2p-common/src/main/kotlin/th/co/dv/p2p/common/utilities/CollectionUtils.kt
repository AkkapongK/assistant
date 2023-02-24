package th.co.dv.p2p.common.utilities

import net.corda.core.internal.castIfPossible
import net.corda.core.node.services.vault.*
import net.corda.core.node.services.vault.Builder.predicate
import java.util.*
import kotlin.reflect.KProperty1

/**
 * Return the power set element in the given list
 * and return empty set if the list is empty
 * powerset({}) == {{}}
 * powerset({1}) == {{1}, {}}
 * powerset({1, 2}) == {{1, 2}, {1}, {2}, {}}
 * 
 * TODO: criteriaQuerySelect out using tailrec to optimise recursion
 */
fun <T> Collection<T>.powerset(revertSort: Boolean = false): Set<Set<T>> = when {
    isEmpty() -> setOf(setOf())
    else -> {
        var result = drop(1).powerset().let { num -> num + num.map { it + first() } }

        if(revertSort) {
            result = result.sortedByDescending { it.size }.toSet()
        }

        result
    }
}

/**
 * Return true if collection is null OR empty
 */
fun <T> Collection<T>?.isNullOrEmpty(): Boolean {
    return this?.isEmpty() ?: true
}

fun <T> Collection<T?>.hasNullOrEmpty(): Boolean {
    return this.contains(null) || this.isEmpty()
}

fun <T, E> getKeysByValue(map: Map<T, E>, value: E): Set<T> {
    val keys = HashSet<T>()
    for ((key, value1) in map) {
        if (Objects.equals(value, value1)) {
            keys.add(key)
        }
    }
    return keys
}

/**
 * Splits this collection into a list of lists each not exceeding the given size.
 * The last list in the resulting list may have less elements than the given size.
 * val words = "one two three four five six seven eight nine ten".split(' ')
 * val chunks = words.chunked(3)
 * println(chunks) // [[one, two, three], [four, five, six], [seven, eight, nine], ten]
 */
fun <T> List<T>.chunked(chunkSize: Int): List<List<T>> {
    val capacity = (this.size + chunkSize - 1) / chunkSize
    val list = ArrayList<ArrayList<T>>(capacity)
    for (i in this.indices) {
        if (i % chunkSize == 0) {
            list.add(ArrayList(chunkSize))
        }
        list.last().add(this[i])
    }
    return list
}

/**
 * Generics to handle string to column comparison.
 * 1. If input is a string, the search criteria has the posssibility of wanting to query data that
 * are isNull, notNull, true, false, or column<R>equal(value as R)
 * 2. Else we handle with generic where column<R>.equal(value as R)
 *
 * To handle #1 we do a lot of assumption and if/else to build our operator expression
 */
@Suppress("UNCHECKED_CAST")
inline fun <O, reified R> KProperty1<O, R?>.isInOrEqual(value: Any): CriteriaExpression.ColumnPredicateExpression<O, R> {
    val isNull = predicate(ColumnPredicate.NullExpression(NullOperator.IS_NULL))
    val notNull = predicate(ColumnPredicate.NullExpression(NullOperator.NOT_NULL))

    // We use castIfPossible(obj: Any) and we provide 'true' as the object (any Boolean works)
    // If column casted is not null, assign to true (castable to boolean), else false.
    val columnIsBoolean = R::class.java.castIfPossible(true)?.let { true } ?: false

    val valueIsNonEmptyCollection = if (value is Collection<*> && value.filterIsInstance<R>().size == value.size && value.isNotEmpty()) value.filterIsInstance<R>() else null

    return when {
    // When value is String, we check if intention is to get IS_NULL / NOT_NULL
        value is String && value.uppercase() == NullOperator.IS_NULL.name -> isNull
        value is String && value.uppercase() == NullOperator.NOT_NULL.name -> notNull

    // If value is String but the column being compared to is Boolean
    // then we do a casting to cast string to Boolean
    // We know value.toBoolean as R casting will always work because we already check columnIsBoolean
        value is String && columnIsBoolean -> predicate(ColumnPredicate.EqualityComparison(EqualityComparisonOperator.EQUAL, value.toBoolean() as R))

    // Value is Collection<R>, then we do `in` operation
        valueIsNonEmptyCollection.isNullOrEmpty().not() -> predicate(ColumnPredicate.CollectionExpression(CollectionOperator.IN, value as Collection<R>))

    // We know that the column type is R, but we don't know the type of value: Any,
    // So we try to force cast to R. Dangerous. Be careful.
        else -> predicate(ColumnPredicate.EqualityComparison(EqualityComparisonOperator.EQUAL, value as R))
    }
}


/**
 * Combination function
 *
 * The method for get the combination of otem in list
 *
 * example: input is [1,2,3,4,5]
 *          size is 3
 *          the result are [[1,2,3], [1,2,4], [1,2,5], [1,3,4], [1,3,5], [2,3,4], [2,3,5], [3,4,5]]
 *
 * Note: If size equals zero we return set of empty list
 *
 * @param size: the size of group combination result
 */
fun <T> Collection<T>.combination(size: Int): Set<List<T>> {
    return when(size > 0) {
        true -> completedListProcess(size, mutableListOf(emptyList())).toSet()
        false -> setOf(emptyList())
    }



}

fun <T> Collection<T>.completedListProcess(size: Int, outputs: MutableList<List<T>>): MutableList<List<T>> {
    val allItem = this.toList()
    var newOutputs = mutableListOf<List<T>>()
    outputs.forEach {
        val eachList = it.toList()

        //get available list
        val availableList = eachList.availableList(allItem, size)
        // Update output list
        availableList.forEach { data ->
            newOutputs.add(eachList + listOf(data))
        }
    }

    if (newOutputs.isEmpty().not()) {
        if (newOutputs.first().size < size) {
            //recursive
            newOutputs = allItem.completedListProcess(size, newOutputs)
        }
    }
    return newOutputs

}



fun <T> Collection<T>.availableList(allList: List<T>, size: Int): List<T> {
    val currentList = this.toMutableList()
    val currentSize = this.size

    // Get needed list size
    val numberThatNeed = size - currentSize

    // the list already completed
    if (numberThatNeed <= 0) return emptyList()

    val lastItem = if (currentList.isEmpty().not()) currentList.last() else null

    // Find index of last in all list
    val lastIndex = when (lastItem == null) {
        true -> 0
        false -> allList.indexOf(lastItem)+1
    }

    // Drop item from start to lasted of current item from all list
    val newAvailable = allList.drop(lastIndex)
    return when (newAvailable.size < numberThatNeed) {
        true -> emptyList()
        false -> newAvailable
    }
}