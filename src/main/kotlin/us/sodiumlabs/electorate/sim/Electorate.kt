package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import us.sodiumlabs.electorate.StringWrapper
import us.sodiumlabs.electorate.Tuple
import us.sodiumlabs.electorate.generateRandomBigDecimal
import java.math.BigDecimal
import java.util.Objects
import java.util.Optional
import java.util.Random
import java.util.stream.Collectors
import java.util.stream.IntStream

fun generateElectorate(random: Random, policies: List<Policy>, electorCount: Int, maxCandidateCount: Int): Electorate {

    val voterRandom = Random(random.nextLong())
    val voters = IntStream.range(0, electorCount)
        .mapToObj { Voter(generateRandomStances(Random(voterRandom.nextLong()), policies), generateRandomBigDecimal(voterRandom)) }
        .collect(Collectors.toList())

    val candidates = generateCandidatesFromVoters(voters, maxCandidateCount)

    return Electorate(voters, candidates)
}

fun generateCandidatesFromVoters(voters: List<Voter>, maxCandidateCount: Int): List<Candidate> {
    val candidates = ArrayList<Candidate>()
    for (i in 1..maxCandidateCount) {
        val potentialCandidate = getVoterMostLikelyToRun(voters, candidates)
        if (!potentialCandidate.isPresent) break
        candidates.add(Candidate(potentialCandidate.get()))
    }
    return candidates
}

private fun getVoterMostLikelyToRun(voters: List<Voter>, candidates: List<Candidate>): Optional<Voter> {
    return voters.stream()
        .map { v -> Tuple(v, v.calculateCurrentRunningPropensity(candidates)) }
        .filter { t -> t.t > BigDecimal.ZERO }
        .max { t1, t2 -> t1.t.compareTo(t2.t) }
        .map { t -> t.s }
}

private fun generateRandomStances(random: Random, policies: List<Policy>): List<Stance> {
    return policies.stream()
        .map { p -> Stance(p, generateRandomBigDecimal(random)) }
        .collect(Collectors.toList())
}

open class Electorate(private val electorate: List<Voter>, val candidates: List<Candidate>) {

    open fun <B> poll(votingStrategy: VotingStrategy<B>): List<B> where B: Ballot {
        return poll(votingStrategy, candidates)
    }

    open fun <B> poll(votingStrategy: VotingStrategy<B>, overrideCandidates: List<Candidate>): List<B> where B: Ballot {
        return electorate.stream()
            .map { t: Voter -> votingStrategy.accept(t, overrideCandidates) }
            .collect(Collectors.toList())
    }

    fun calculateRegret(candidate: Optional<Candidate>): RegretMetrics {
        val utilityMap = HashMap<Candidate, BigDecimal>()
        var maximumUtility = BigDecimal.ZERO
        var minimumUtility = BigDecimal.ONE

        candidates.forEach { c ->
            val utility = calculateCandidateUtility(c)
            if (utility > maximumUtility) maximumUtility = utility
            if (utility < minimumUtility) minimumUtility = utility
            utilityMap[c] = utility
        }

        return candidate.map { c ->
            val rawUtility = utilityMap.getOrElse(c) { calculateCandidateUtility(c) }
            val regret = (maximumUtility - rawUtility).max(BigDecimal.ZERO)
            val normalizedRegret = (regret / (maximumUtility - minimumUtility)).min(BigDecimal.ONE).max(BigDecimal.ZERO)

            RegretMetrics(rawUtility, regret, normalizedRegret)
        }.orElseGet { IndeterminateRegretMetrics() }
    }

    private fun calculateCandidateUtility(candidate: Candidate): BigDecimal {
        return electorate.stream()
            .map { v -> v.calculateCandidateUtility(candidate) }
            .collect(BigDecimalAverageCollector())
    }

    fun toJson(): JsonObject {
        val outElectorate = JsonArray()
        electorate.forEach { v -> outElectorate.add(v.toJson()) }

        val outCandidates = JsonArray()
        candidates.forEach { c -> outCandidates.add(c.toJson()) }

        val out = JsonObject()
        out.add("electorate", outElectorate)
        out.add("candidates", outCandidates)

        return out
    }
}

class Voter(val stances: List<Stance>, private val runningPropensity: BigDecimal) {
    fun calculateCandidateUtility(candidate: Candidate): BigDecimal {
        return stances.stream()
            .map { s -> BigDecimal.ONE - (candidate.getStance(s.policy).value - s.value).abs() }
            .collect(BigDecimalAverageCollector())
    }

    fun calculateCurrentRunningPropensity(candidates: List<Candidate>): BigDecimal {
        return runningPropensity - calculateMaxCandidateUtility(candidates)
    }

    private fun calculateMaxCandidateUtility(candidates: List<Candidate>): BigDecimal {
        return candidates.stream()
            .map { c -> calculateCandidateUtility(c) }
            .max { o1, o2 -> o1.compareTo(o2) }
            .orElse(BigDecimal.ZERO)
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

    constructor(voter: Voter): this(voter.stances)

    init {
        val stanceMapBuilder = HashMap<Policy, Stance>()
        stanceList.forEach { s -> stanceMapBuilder[s.policy] = s }
        stances = ImmutableMap.copyOf(stanceMapBuilder)
    }

    fun getStance(policy: Policy): Stance {
        return stances.getOrDefault(policy, NULL_STANCE)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Candidate) return false

        for (entry in stances.entries) {
            if (!other.stances.containsKey(entry.key) || other.stances[entry.key] != entry.value) return false
        }

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(stances)
    }

    override fun toString(): String {
        return toJson().toString()
    }

    fun toJson(): JsonObject {
        val out = JsonObject()
        stances.forEach { s -> out.addProperty(s.key.toString(), s.value.value) }
        return out
    }
}

class Stance(val policy: Policy, val value: BigDecimal)

val NULL_STANCE = Stance(Policy("null"), BigDecimal.valueOf(0.5))

class Policy(s: String) : StringWrapper(s)

interface VotingStrategy<B> where B : Ballot {
    fun accept(voter: Voter, candidates: List<Candidate>): B
}

interface Ballot
