package us.sodiumlabs.electorate.sim

import com.google.common.base.Objects
import java.math.BigDecimal
import java.util.Optional
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

fun wrap(bigDecimal: BigDecimal): BigDecimalWrapper {
    return RealBigDecimalWrapper(bigDecimal)
}

fun nan(): BigDecimalWrapper {
    return BigDecimalWrapper.NAN_WRAPPER
}

abstract class BigDecimalWrapper {
    companion object {
        internal val NAN_WRAPPER = NaNBigDecimalWrapper()
    }

    abstract fun ifPresent(consumer: Consumer<BigDecimal>)

    abstract fun <T> map(operation: Function<BigDecimal, T>): Optional<T>

    abstract fun <T> bimap(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T>

    internal abstract fun <T> acceptRealBiMap(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T>

    abstract fun operate(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper

    internal abstract fun acceptRealOperate(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper

    abstract fun compare(other: BigDecimalWrapper, nanHigh: Boolean = true): Int

    internal abstract fun acceptRealCompare(other: RealBigDecimalWrapper, nanHigh: Boolean): Int

    internal abstract fun acceptNanCompare(other: NaNBigDecimalWrapper, nanHigh: Boolean): Int
}

internal class RealBigDecimalWrapper(private val v: BigDecimal): BigDecimalWrapper() {
    override fun ifPresent(consumer: Consumer<BigDecimal>) = consumer.accept(v)

    override fun <T> map(operation: Function<BigDecimal, T>): Optional<T> {
        return Optional.ofNullable(operation.apply(v))
    }

    override fun <T> bimap(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T> {
        return other.acceptRealBiMap(this, operation)
    }

    override fun <T> acceptRealBiMap(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T> {
        return Optional.ofNullable(operation.apply(other.v, v))
    }

    override fun operate(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper {
        return other.acceptRealOperate(this, operation)
    }

    override fun acceptRealOperate(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper {
        return wrap(operation.apply(other.v, v))
    }

    override fun compare(other: BigDecimalWrapper, nanHigh: Boolean): Int {
        return other.acceptRealCompare(this, nanHigh)
    }

    override fun acceptRealCompare(other: RealBigDecimalWrapper, nanHigh: Boolean): Int {
        return other.v.compareTo(v)
    }

    override fun acceptNanCompare(other: NaNBigDecimalWrapper, nanHigh: Boolean): Int {
        return if (nanHigh) 1 else -1
    }

    override fun toString(): String {
        return v.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other is RealBigDecimalWrapper && Objects.equal(v, other.v)
    }

    override fun hashCode(): Int {
        return v.hashCode()
    }
}

internal class NaNBigDecimalWrapper: BigDecimalWrapper() {
    override fun ifPresent(consumer: Consumer<BigDecimal>) {
        // do nothing
    }

    override fun <T> map(operation: Function<BigDecimal, T>): Optional<T> = Optional.empty()

    override fun <T> bimap(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T> = Optional.empty()

    override fun <T> acceptRealBiMap(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, T>
    ): Optional<T> = Optional.empty()

    override fun operate(
        other: BigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper = this

    override fun acceptRealOperate(
        other: RealBigDecimalWrapper,
        operation: BiFunction<BigDecimal, BigDecimal, BigDecimal>
    ): BigDecimalWrapper = this

    override fun compare(other: BigDecimalWrapper, nanHigh: Boolean): Int {
        return other.acceptNanCompare(this, nanHigh)
    }

    override fun acceptRealCompare(other: RealBigDecimalWrapper, nanHigh: Boolean): Int {
        return if (nanHigh) -1 else 1
    }

    override fun acceptNanCompare(other: NaNBigDecimalWrapper, nanHigh: Boolean): Int {
        return 0
    }

    override fun toString(): String {
        return "Not a Number!"
    }

    override fun equals(other: Any?): Boolean {
        return other is NaNBigDecimalWrapper
    }

    override fun hashCode() = 0
}
