package us.sodiumlabs.electorate.sim

import com.google.common.base.Strings
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import java.math.BigDecimal
import java.util.function.Function

open class RegretMetrics(
    val rawUtility: BigDecimalWrapper,
    val regret: BigDecimalWrapper,
    val normalizedRegret: BigDecimalWrapper,
    val indeterminate: Boolean = false
) {
    override fun toString(): String {
        return if (indeterminate) {
            "election was indeterminate"
        } else {
            "raw utility: ${padBigDecimal(rawUtility)} " +
                "regret: ${padBigDecimal(regret)} " +
                "normalized regret: $normalizedRegret"
        }
    }
}

fun indeterminate(): RegretMetrics {
    return RegretMetrics(nan(), nan(), nan(), true)
}

class RegretStatistics(private val name: ElectoralSystemName, regretMetrics: Collection<RegretMetrics>) {
    private val rawUtilityStatistics: Statistics
    private val regretStatistics: Statistics
    private val normalizedRegretStatistics: Statistics
    private val indeterminateFraction: BigDecimal

    init {
        rawUtilityStatistics = Statistics(regretMetrics, false) { r -> r.rawUtility }
        regretStatistics = Statistics(regretMetrics) { r -> r.regret }
        normalizedRegretStatistics = Statistics(regretMetrics) { r -> r.normalizedRegret }
        var indeterminate = 0
        regretMetrics.filter { it.indeterminate }.forEach { indeterminate++ }
        indeterminateFraction = BigDecimal(indeterminate).divide(BigDecimal(regretMetrics.size))
    }

    override fun toString(): String {
        return "= $name =\n" +
            "== raw utility ==\n" +
            "$rawUtilityStatistics\n" +
            "== regret ==\n" +
            "$regretStatistics\n" +
            "== normalized regret ==\n" +
            "$normalizedRegretStatistics\n" +
            "== indeterminate fraction ==\n" +
            indeterminateFraction
    }
}

class Statistics(
    regretMetrics: Collection<RegretMetrics>,
    nanHigh: Boolean = true,
    metricAccessor: Function<RegretMetrics, BigDecimalWrapper>
) {
    private val p0: BigDecimalWrapper
    private val p10: BigDecimalWrapper
    private val p50: BigDecimalWrapper
    private val p90: BigDecimalWrapper
    private val p100: BigDecimalWrapper
    private val mean: BigDecimalWrapper

    init {
        val sortedMetrics = regretMetrics.sortedWith { left, right ->
            metricAccessor.apply(left).compare(metricAccessor.apply(right), nanHigh)
        }
        p0 = metricAccessor.apply(sortedMetrics[0])
        p10 = metricAccessor.apply(sortedMetrics[sortedMetrics.size / 10])
        p50 = metricAccessor.apply(sortedMetrics[sortedMetrics.size / 2])
        p90 = metricAccessor.apply(sortedMetrics[sortedMetrics.size - sortedMetrics.size / 10])
        p100 = metricAccessor.apply(sortedMetrics[sortedMetrics.lastIndex])
        mean = regretMetrics.stream()
            .map { r -> metricAccessor.apply(r) }
            .collect(BigDecimalAverageCollector())
    }

    override fun toString(): String {
        return "mean: ${padBigDecimal(mean)} " +
            "p0: ${padBigDecimal(p0)} " +
            "p10: ${padBigDecimal(p10)} " +
            "p50: ${padBigDecimal(p50)} " +
            "p90: ${padBigDecimal(p90)} " +
            "p100: ${padBigDecimal(p100)}"
    }
}

fun padBigDecimal(b: BigDecimalWrapper): String {
    return Strings.padEnd("$b;", 13, ' ')
}
