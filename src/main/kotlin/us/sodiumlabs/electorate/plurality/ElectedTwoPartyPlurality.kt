package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import us.sodiumlabs.electorate.sim.*
import java.math.BigDecimal

class ElectedTwoPartyPlurality: Plurality() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Elected Two Party Plurality")
        val CONTENTION_STRATEGY = ContentionVotingStrategy()
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        // Elect a contentious policy
        val policyPolling = electorate.poll(CONTENTION_STRATEGY)
        val policyContentionMap = HashMap<Policy, BigDecimal>()

        for(i in 0 until policyPolling.size - 1) {
            for(j in i until policyPolling.size) {
                val v1 = policyPolling[i]
                val v2 = policyPolling[j]

                for((key, value) in v1.stances) {
                    val s1 = value.value
                    val s2 = v2.stances.getOrDefault(key, NULL_STANCE).value

                    policyContentionMap.compute(key, { _, it ->
                        val diff = (s1 - s2).abs()

                        if(it == null) {
                            diff
                        } else {
                            it + diff
                        }
                    } )
                }
            }
        }

        val electedPolicy = policyContentionMap.entries.stream()
                .reduce({ a,b ->
                    if(a.value > b.value) {
                        a
                    } else {
                        b
                    }
                })
                .orElseThrow({RuntimeException("Should elect a policy!")})
                .key

        // Run Primaries based on the contentious policy.
        val primaryBallots = electorate.poll(PrimaryVotingStrategy(electedPolicy))
        val forPartyBallotCount = HashMultiset.create<Candidate>()
        val againstPartyBallotCount = HashMultiset.create<Candidate>()
        primaryBallots
                .filter { it.forParty }
                .mapTo(forPartyBallotCount) { it.candidate }
        primaryBallots
                .filter { !it.forParty }
                .mapTo(againstPartyBallotCount) { it.candidate }

        val forPartyCandidate = findFirst(forPartyBallotCount)
        val againstPartyCandidate = findFirst(againstPartyBallotCount)

        // Run the actual election. The winner of each party might pull voters of the opposing party, which is why we
        // did not declare the winner based on the number of votes cast for each party.
        val ballots = electorate.poll(VOTING_STRATEGY, ImmutableList.of(forPartyCandidate, againstPartyCandidate))
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.mapTo(ballotCount) { it.candidate }

        return findFirst(ballotCount)
    }

    class ContentionVotingStrategy : VotingStrategy<PolicyContentionBallot> {
        override fun accept(voter: Voter, candidates: List<Candidate>): PolicyContentionBallot {
            return PolicyContentionBallot(voter.stances)
        }
    }

    class PolicyContentionBallot(stanceList: List<Stance>): Ballot {
        val stances: Map<Policy, Stance>

        init {
            val stanceInit = HashMap<Policy, Stance>()
            stanceList.forEach { stanceInit.put(it.policy, it) }
            stances = ImmutableMap.copyOf(stanceInit)
        }
    }

    class PrimaryVotingStrategy(private val policy: Policy) : VotingStrategy<PrimaryBallot> {
        override fun accept(voter: Voter, candidates: List<Candidate>): PrimaryBallot {
            var maximumUtility = BigDecimal.valueOf(-1.0)
            var outCandidate: Candidate? = null

            for(c in candidates) {
                val utility = voter.calculateCandidateUtility(c)

                if(utility > maximumUtility) {
                    maximumUtility = utility
                    outCandidate = c
                }
            }

            var forParty = false
            voter.stances
                    .filter { it.policy == policy }
                    .forEach { forParty = it.value > BigDecimal.valueOf(0.5) }

            if(outCandidate == null) throw RuntimeException("No candidates with positive utility for voter!")

            return PrimaryBallot(outCandidate, forParty)
        }
    }

    class PrimaryBallot(val candidate: Candidate, val forParty: Boolean): Ballot
}