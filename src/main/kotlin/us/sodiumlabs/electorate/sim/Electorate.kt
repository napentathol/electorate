package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import us.sodiumlabs.electorate.PRECISION
import us.sodiumlabs.electorate.StringWrapper
import us.sodiumlabs.electorate.Tuple
import us.sodiumlabs.electorate.generateRandomBigDecimal
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
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
        .filter { t -> t.t.mapToOptional { v -> v > BigDecimal.ZERO }.orElse(false) }
        .max { t1, t2 -> t1.t.compare(t2.t) }
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
        val utilityMap = HashMap<Candidate, BigDecimalWrapper>()
        var maximumUtility = BigDecimal.ZERO
        var minimumUtility = BigDecimal.ONE

        candidates.forEach { c ->
            val utility = calculateCandidateUtility(c)
            utility.ifPresent {
                if (it > maximumUtility) maximumUtility = it
                if (it < minimumUtility) minimumUtility = it
            }
            utilityMap[c] = utility
        }

        return candidate.map { c ->
            val rawUtility = utilityMap.getOrElse(c) { calculateCandidateUtility(c) }
            val regret = rawUtility
                .mapToOptional { (maximumUtility - it).max(BigDecimal.ZERO) }
                .map(::wrap)
                .orElseGet(::nan)
            val normalizedRegret = if (maximumUtility.compareTo(minimumUtility) != 0) {
                regret.mapToOptional {
                    (it / (maximumUtility - minimumUtility)).min(BigDecimal.ONE).max(BigDecimal.ZERO)
                }.map(::wrap).orElseGet(::nan)
            } else {
                nan()
            }

            RegretMetrics(rawUtility, regret, normalizedRegret)
        }.orElseGet { indeterminate() }
    }

    private fun calculateCandidateUtility(candidate: Candidate): BigDecimalWrapper {
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

class Voter(val stances: List<Stance>, private val runningPropensity: BigDecimalWrapper) {

    companion object {
        private val SQRT_CONTEXT = MathContext(PRECISION, RoundingMode.FLOOR)
    }

    private val policyScale: BigDecimal

    init {
        policyScale = BigDecimal.valueOf(stances.size.toLong()).sqrt(SQRT_CONTEXT)
    }

    fun calculateCandidateUtility(candidate: Candidate): BigDecimalWrapper {
        return stances.stream()
            .map { s ->
                candidate.getStance(s.policy).value.biMap(s.value) { left, right ->
                    BigDecimal.ONE - (left - right).abs()
                }
            }.map {
                it.map { x -> x * x }
            }.reduce(zero()) { x, y ->
                x.biMap(y) { l, r -> l + r }
            }.map { it.sqrt(SQRT_CONTEXT) / policyScale }
    }

    fun calculateCurrentRunningPropensity(candidates: List<Candidate>): BigDecimalWrapper {
        val maxCandidateUtility = calculateMaxCandidateUtility(candidates)
        return runningPropensity.biMap(maxCandidateUtility) {
            left, right -> left - right
        }
    }

    private fun calculateMaxCandidateUtility(candidates: List<Candidate>): BigDecimalWrapper {
        return candidates.stream()
            .map { c -> calculateCandidateUtility(c) }
            .max { o1, o2 -> o1.compare(o2) }
            .orElse(zero())
    }

    fun toJson(): JsonObject {
        val out = JsonObject()
        stances.forEach { s ->
            out.addProperty(s.policy.toString(), s.value.toString())
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
        stances.forEach { s -> out.addProperty(s.key.toString(), s.value.value.toString()) }
        return out
    }
}

class Stance(val policy: Policy, val value: BigDecimalWrapper)

val NULL_STANCE = Stance(Policy("null"), nan())

class Policy(s: String) : StringWrapper(s)

interface VotingStrategy<B> where B : Ballot {
    fun accept(voter: Voter, candidates: List<Candidate>): B
}

interface Ballot
