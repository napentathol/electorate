package us.sodiumlabs.electorate.sim

import com.google.common.base.Strings
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multiset
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import us.sodiumlabs.electorate.StringWrapper
import java.io.FileWriter
import java.math.BigDecimal
import kotlin.streams.toList

class ElectionSim(private val electoralSystems: List<ElectoralSystem>) {
    private val regretMatrix: Multimap<ElectoralSystemName, RegretMetrics> = HashMultimap.create()

    private val seenElectorates = ArrayList<Electorate>()

    fun runElectionSuite(electorate: Electorate) {
        println("=== New Electorate ===")
        seenElectorates.add(electorate)
        electoralSystems.forEach { e ->
            val regret = electorate.calculateRegret(e.produceCandidate(electorate))
            println("${Strings.padEnd("Regret for ${e.getSystemName()}: ", 50, ' ')}$regret")
            regretMatrix.put(e.getSystemName(), regret)
        }
        println()
    }

    fun printRegret() {
        FileWriter("/var/tmp/output.json").use { writer ->
            val gson = GsonBuilder().create()
            gson.toJson(toJson(), writer)
        }
        regretMatrix.keySet().stream()
                .sorted()
                .forEach { k ->
                    println(RegretStatistics(k, regretMatrix.get(k)).toString())
                    println()
                }
    }

    private fun toJson(): JsonObject {
        val seenElectoratesOut = JsonArray()
        seenElectorates.forEach { e -> seenElectoratesOut.add(e.toJson()) }

        val regretStats = JsonObject()
        regretMatrix.keySet().forEach { k -> regretStats.add(k.toString(), regretStatsToJson(k)) }

        val out = JsonObject()
        out.add("seenElectorates", seenElectoratesOut)
        out.add("regretStatistics", regretStats)
        return out
    }

    private fun regretStatsToJson(name: ElectoralSystemName): JsonObject {
        val out = JsonObject()

        out.addProperty("mean normalized regret", regretMatrix.get(name).stream()
                .map { m -> m.normalizedRegret }
                .collect(BigDecimalAverageCollector()))
        out.addProperty("mean regret", regretMatrix.get(name).stream()
                .map { m -> m.regret }
                .collect(BigDecimalAverageCollector()))
        out.addProperty("mean raw utility", regretMatrix.get(name).stream()
                .map { m -> m.rawUtility }
                .collect(BigDecimalAverageCollector()))

        return out
    }
}

interface ElectoralSystem {
    fun produceCandidate(electorate: Electorate): Candidate

    fun getSystemName(): ElectoralSystemName

    fun findFirst(count: Multiset<Candidate>): Candidate {
        return count.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .findFirst()
                .orElseThrow { RuntimeException("There should be a candidate with votes!") }
                .element
    }

    fun sortedCandidateList(count: Multiset<Candidate>): List<Candidate> {
        return count.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .map { it.element }
                .toList()
    }
}

class ElectoralSystemName(s: String) : StringWrapper(s)