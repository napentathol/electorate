package us.sodiumlabs.electorate

import com.google.common.base.Strings
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Collections
import java.util.Random
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector

const val PRECISION = 10

fun generateRandomBigDecimal(random: Random): BigDecimal {
    return BigDecimal(random.nextInt(1_000_000_000)).divide(BigDecimal(1_000_000_000), PRECISION, RoundingMode.FLOOR)
}

abstract class StringWrapper(private val s: String) : Comparable<StringWrapper> {
    override fun compareTo(other: StringWrapper): Int = s.compareTo(other.s)

    override fun hashCode(): Int = s.hashCode()

    override fun equals(other: Any?): Boolean = other is StringWrapper && other.s == s

    override fun toString(): String = s
}

class Tuple<S, T>(val s: S, val t: T)

class BigDecimalAverageCollector : Collector<BigDecimal, BigDecimalAverageCollector.BigDecimalAccumulator, BigDecimal> {

    override fun supplier(): Supplier<BigDecimalAccumulator> {
        return Supplier { BigDecimalAccumulator() }
    }

    override fun accumulator(): BiConsumer<BigDecimalAccumulator, BigDecimal> {
        return BiConsumer { obj, successRate -> obj.add(successRate) }
    }

    override fun combiner(): BinaryOperator<BigDecimalAccumulator> {
        return BinaryOperator { obj, another -> obj.combine(another) }
    }

    override fun finisher(): Function<BigDecimalAccumulator, BigDecimal> {
        return Function { it.average() }
    }

    override fun characteristics(): Set<Collector.Characteristics> {
        return Collections.emptySet()
    }

    class BigDecimalAccumulator() {
        private var sum = BigDecimal.ZERO
        private var count = BigDecimal.ZERO

        constructor(sum: BigDecimal, count: BigDecimal) : this() {
            this.sum = sum
            this.count = count
        }

        fun average(): BigDecimal {
            if (count == BigDecimal.ZERO) throw RuntimeException("Count must not be zero!")
            return sum.divide(count, PRECISION, RoundingMode.HALF_UP)
        }

        fun combine(another: BigDecimalAccumulator): BigDecimalAccumulator {
            return BigDecimalAccumulator(
                    sum.add(another.sum),
                    count.add(another.count)
            )
        }

        fun add(successRate: BigDecimal) {
            count = count.add(BigDecimal.ONE)
            sum = sum.add(successRate)
        }
    }
}