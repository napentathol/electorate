package us.sodiumlabs.electorate.sim

import com.google.common.base.Objects
import java.math.BigDecimal
import java.util.function.Consumer

fun wrap(bigDecimal: BigDecimal): RegretBigDecimalWrapper {
    return RealBigDecimalWrapper(bigDecimal)
}

fun nan(): RegretBigDecimalWrapper {
    return RegretBigDecimalWrapper.NAN_WRAPPER
}

abstract class RegretBigDecimalWrapper {
    companion object {
        internal val NAN_WRAPPER = NaNBigDecimalWrapper()
    }

    abstract fun ifPresent(consumer: Consumer<BigDecimal>)

    abstract fun compare(other: RegretBigDecimalWrapper, nanHigh: Boolean = true): Int

    internal abstract fun acceptRealCompare(other: RealBigDecimalWrapper, nanHigh: Boolean): Int

    internal abstract fun acceptNanCompare(other: NaNBigDecimalWrapper, nanHigh: Boolean): Int
}

internal class RealBigDecimalWrapper(private val v: BigDecimal): RegretBigDecimalWrapper() {
    override fun ifPresent(consumer: Consumer<BigDecimal>) = consumer.accept(v)

    override fun compare(other: RegretBigDecimalWrapper, nanHigh: Boolean): Int {
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

internal class NaNBigDecimalWrapper: RegretBigDecimalWrapper() {
    override fun ifPresent(consumer: Consumer<BigDecimal>) {
        // do nothing
    }

    override fun compare(other: RegretBigDecimalWrapper, nanHigh: Boolean): Int {
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
