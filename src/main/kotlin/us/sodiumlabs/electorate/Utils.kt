package us.sodiumlabs.electorate

import us.sodiumlabs.electorate.sim.BigDecimalWrapper
import us.sodiumlabs.electorate.sim.nan
import us.sodiumlabs.electorate.sim.wrap
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

fun generateRandomBigDecimal(random: Random): BigDecimalWrapper {
    return wrap(
        BigDecimal(random.nextInt(1_000_000_000))
            .divide(BigDecimal(1_000_000_000), PRECISION, RoundingMode.FLOOR)
    )
}

abstract class StringWrapper(private val s: String) : Comparable<StringWrapper> {
    override fun compareTo(other: StringWrapper): Int = s.compareTo(other.s)

    override fun hashCode(): Int = s.hashCode()

    override fun equals(other: Any?): Boolean = other is StringWrapper && other.s == s

    override fun toString(): String = s
}

class Tuple<S, T>(val s: S, val t: T)

class BigDecimalAverageCollector : Collector<BigDecimalWrapper, BigDecimalAverageCollector.BigDecimalAccumulator, BigDecimalWrapper> {

    override fun supplier(): Supplier<BigDecimalAccumulator> {
        return Supplier { BigDecimalAccumulator() }
    }

    override fun accumulator(): BiConsumer<BigDecimalAccumulator, BigDecimalWrapper> {
        return BiConsumer { accumulator, successRate ->
            successRate.ifPresent { accumulator.add(it) }
        }
    }

    override fun combiner(): BinaryOperator<BigDecimalAccumulator> {
        return BinaryOperator { obj, another -> obj.combine(another) }
    }

    override fun finisher(): Function<BigDecimalAccumulator, BigDecimalWrapper> {
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

        fun average(): BigDecimalWrapper {
            return if (count == BigDecimal.ZERO) {
                nan()
            } else {
                wrap(sum.divide(count, PRECISION, RoundingMode.HALF_UP))
            }
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
