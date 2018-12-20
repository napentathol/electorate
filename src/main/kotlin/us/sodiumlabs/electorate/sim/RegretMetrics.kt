package us.sodiumlabs.electorate.sim

import com.google.common.base.Strings
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import java.math.BigDecimal
import java.util.function.Function


class RegretMetrics(val rawUtility: BigDecimal, val regret: BigDecimal, val normalizedRegret: BigDecimal) {
    override fun toString(): String {
        return "raw utility: ${padBigDecimal(rawUtility)} " +
                "regret: ${padBigDecimal(regret)} " +
                "normalized regret: $normalizedRegret"
    }
}

class RegretStatistics {
    private val name: ElectoralSystemName
    private val rawUtilityStatistics: Statistics
    private val regretStatistics: Statistics
    private val normalizedRegretStatistics: Statistics

    constructor(name: ElectoralSystemName, regretMetrics: Collection<RegretMetrics>) {
        this.name = name
        rawUtilityStatistics = Statistics(regretMetrics, Function { r -> r.rawUtility })
        regretStatistics = Statistics(regretMetrics, Function { r -> r.regret })
        normalizedRegretStatistics = Statistics(regretMetrics, Function { r -> r.normalizedRegret })
    }

    override fun toString(): String {
        return "= $name =\n" +
                "== raw utility ==\n" +
                "$rawUtilityStatistics\n" +
                "== regret ==\n" +
                "$regretStatistics\n" +
                "== normalized regret ==\n" +
                "$normalizedRegretStatistics"
    }
}

class Statistics {
    private val p0: BigDecimal
    private val p10: BigDecimal
    private val p50: BigDecimal
    private val p90: BigDecimal
    private val p100: BigDecimal
    private val mean: BigDecimal

    constructor(regretMetrics: Collection<RegretMetrics>, metricAccessor: Function<RegretMetrics, BigDecimal>) {
        val sortedMetrics = regretMetrics.sortedBy { r -> metricAccessor.apply(r) }.toList()

        p0 = metricAccessor.apply(sortedMetrics[0])
        p10 = metricAccessor.apply(sortedMetrics[sortedMetrics.size/10])
        p50 = metricAccessor.apply(sortedMetrics[sortedMetrics.size/2])
        p90 = metricAccessor.apply(sortedMetrics[sortedMetrics.size - sortedMetrics.size/10])
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

fun padBigDecimal(b: BigDecimal): String {
    return Strings.padEnd("$b;", 13, ' ')
}