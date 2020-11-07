package us.sodiumlabs.electorate.sim

import com.google.common.collect.Collections2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class RegretBigDecimalWrapperTest {

    @Test
    fun `test compare results`() {
        val zero = wrap(BigDecimal.ZERO)
        val nan = nan()

        assertEquals(1, nan.compare(zero))
        assertEquals(-1, zero.compare(nan))

        assertEquals(1, nan.compare(zero, true))
        assertEquals(-1, zero.compare(nan, true))

        assertEquals(-1, nan.compare(zero, false))
        assertEquals(1, zero.compare(nan, false))

        assertEquals(0, nan.compare(nan))
        assertEquals(0, zero.compare(zero))
        assertEquals(0, nan.compare(nan, true))
        assertEquals(0, zero.compare(zero, true))
        assertEquals(0, nan.compare(nan, false))
        assertEquals(0, zero.compare(zero, false))
    }

    @Test
    fun compare() {
        val list = mutableListOf(
            wrap(BigDecimal.ZERO),
            wrap(BigDecimal.ONE),
            wrap(BigDecimal.TEN),
            nan(),
            nan()
        )

        val nanHigh = listOf(
            wrap(BigDecimal.ZERO),
            wrap(BigDecimal.ONE),
            wrap(BigDecimal.TEN),
            nan(),
            nan()
        )
        val nanLow = listOf(
            nan(),
            nan(),
            wrap(BigDecimal.ZERO),
            wrap(BigDecimal.ONE),
            wrap(BigDecimal.TEN)
        )

        @Suppress("UnstableApiUsage")
        val permutations = Collections2.permutations(list)

        permutations.forEach { p ->
            var copy = ArrayList(p)
            copy.sortWith { a, b -> a.compare(b) }
            assertEquals(copy, nanHigh)

            copy = ArrayList(p)
            copy.sortWith { a, b -> a.compare(b, true) }
            assertEquals(copy, nanHigh)

            copy = ArrayList(p)
            copy.sortWith { a, b -> a.compare(b, false) }
            assertEquals(copy, nanLow)
        }
    }
}
