package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import us.sodiumlabs.electorate.StringWrapper
import us.sodiumlabs.electorate.generateRandomBigDecimal
import java.math.BigDecimal
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.collections.HashMap



fun generateElectorate(random: Random, policies: List<Policy>, electorCount: Int, candidateCount: Int): Electorate {

    val voterRandom = Random(random.nextLong())
    val voters = IntStream.range(0, electorCount)
            .mapToObj { Voter(generateRandomStances(Random(voterRandom.nextLong()), policies)) }
            .collect(Collectors.toList())

    val candidateRandom = Random(random.nextLong())
    val candidates = IntStream.range(0, candidateCount)
            .mapToObj { Candidate(generateRandomStances(Random(candidateRandom.nextLong()), policies)) }
            .collect(Collectors.toList())

    return Electorate(voters, candidates)
}

private fun generateRandomStances(random: Random, policies: List<Policy>): List<Stance> {
    return policies.stream()
            .map { p -> Stance(p, generateRandomBigDecimal(random)) }
            .collect(Collectors.toList())
}

class Electorate(private val electorate: List<Voter>, val candidates: List<Candidate>) {

    fun <B> poll(votingStrategy: VotingStrategy<B>): List<B> where B: Ballot {
        return poll(votingStrategy, candidates)
    }

    fun <B> poll(votingStrategy: VotingStrategy<B>, overrideCandidates: List<Candidate>): List<B> where B: Ballot {
        return electorate.stream()
                .map { t: Voter -> votingStrategy.accept(t, overrideCandidates) }
                .collect(Collectors.toList())
    }

    fun calculateRegret(candidate: Candidate) : BigDecimal {
        val utilityMap = HashMap<Candidate, BigDecimal>()
        var maximumUtility = BigDecimal.ZERO

        candidates.forEach { c ->
            val utility = calculateCandidateUtility(c)
            if(utility > maximumUtility) maximumUtility = utility
            utilityMap.put(c, utility)
        }

        return (maximumUtility - utilityMap.getOrElse(candidate, {calculateCandidateUtility(candidate)}))
                .max(BigDecimal.ZERO)
    }

    private fun calculateCandidateUtility(candidate: Candidate): BigDecimal {
        return electorate.stream()
                .map { v -> v.calculateCandidateUtility(candidate) }
                .collect(BigDecimalAverageCollector())
    }

    fun toJson(): JsonObject {
        val outElectorate = JsonArray()
        electorate.forEach { v ->
            outElectorate.add(v.toJson())
        }

        val outCandidates = JsonArray()
        candidates.forEach { c ->
            outCandidates.add(c.toJson())
        }

        val out = JsonObject()
        out.add("electorate", outElectorate)
        out.add("candidates", outCandidates)

        return out
    }
}

class Voter(private val stances: List<Stance>) {
    fun calculateCandidateUtility(candidate: Candidate): BigDecimal {
        return stances.stream()
                .map { s ->
                    BigDecimal.ONE - (candidate.getStance(s.policy).value - s.value).abs()
                }
                .collect(BigDecimalAverageCollector())
    }

    fun toJson(): JsonObject {
        val out = JsonObject()
        stances.forEach { s ->
            out.addProperty(s.policy.toString(), s.value)
        }
        return out
    }
}

class Candidate(stanceList: List<Stance>) {
    private val stances: Map<Policy, Stance>

    init {
        val stanceMapBuilder = HashMap<Policy, Stance>()
        stanceList.forEach { s -> stanceMapBuilder.put(s.policy, s) }
        stances = ImmutableMap.copyOf(stanceMapBuilder)
    }

    fun getStance(policy: Policy) : Stance {
        return stances.getOrDefault(policy, NULL_STANCE)
    }

    fun toJson(): JsonObject {
        val out = JsonObject()
        stances.forEach { s ->
            out.addProperty(s.key.toString(), s.value.value)
        }
        return out
    }
}

class Stance(val policy: Policy, val value: BigDecimal)

private val NULL_STANCE = Stance(Policy("null"), BigDecimal.valueOf(0.5))

class Policy(s: String): StringWrapper(s)

interface VotingStrategy<B> where B : Ballot {
    fun accept(voter: Voter, candidates: List<Candidate>) : B
}

interface Ballot