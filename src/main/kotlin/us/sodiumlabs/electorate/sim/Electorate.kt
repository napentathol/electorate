package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import us.sodiumlabs.electorate.StringWrapper
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
            .map { p -> Stance(p, random.nextDouble()) }
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

    fun calculateRegret(candidate: Candidate) : Double {
        val utilityMap = HashMap<Candidate, Double>()
        var maximumUtility = 0.0

        candidates.forEach { c ->
            val utility = calculateCandidateUtility(c)
            if(utility > maximumUtility) maximumUtility = utility
            utilityMap.put(c, utility)
        }

        return Math.max(maximumUtility - utilityMap.getOrElse(candidate, {calculateCandidateUtility(candidate)}), 0.0)
    }

    private fun calculateCandidateUtility(candidate: Candidate): Double {
        return electorate.stream()
                .mapToDouble { v -> v.calculateCandidateUtility(candidate) }
                .average()
                .orElseThrow { RuntimeException("Electorate should have at least one voter!") }
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
    fun calculateCandidateUtility(candidate: Candidate): Double {
        return stances.stream()
                .mapToDouble { s ->
                    1.0 - Math.abs(candidate.getStance(s.policy).value - s.value)
                }
                .average()
                .orElseThrow { RuntimeException("Voter should have at least one stance!") }
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

class Stance(val policy: Policy, val value: Double)

private val NULL_STANCE = Stance(Policy("null"), 0.5)

class Policy(s: String): StringWrapper(s)

interface VotingStrategy<B> where B : Ballot {
    fun accept(voter: Voter, candidates: List<Candidate>) : B
}

interface Ballot