package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import us.sodiumlabs.electorate.sim.Ballot
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import us.sodiumlabs.electorate.sim.Voter
import us.sodiumlabs.electorate.sim.VotingStrategy
import java.math.BigDecimal

open class Plurality : ElectoralSystem {
    companion object {
        val VOTING_STRATEGY = PluralityVotingStrategy()
        val SYSTEM_NAME = ElectoralSystemName("Plurality - Pure")
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val ballots = electorate.poll(VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.mapTo(ballotCount) { it.candidate }

        return findFirst(ballotCount)
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    class PluralityVotingStrategy : VotingStrategy<PluralityBallot> {
        override fun accept(voter: Voter, candidates: List<Candidate>): PluralityBallot {
            var maximumUtility = BigDecimal.valueOf(-1.0)
            var outCandidate: Candidate? = null

            for (c in candidates) {
                val utility = voter.calculateCandidateUtility(c)

                if (utility > maximumUtility) {
                    maximumUtility = utility
                    outCandidate = c
                }
            }

            if (outCandidate == null) throw RuntimeException("No candidates with positive utility for voter!")

            return PluralityBallot(outCandidate)
        }
    }

    class PluralityBallot(val candidate: Candidate) : Ballot
}