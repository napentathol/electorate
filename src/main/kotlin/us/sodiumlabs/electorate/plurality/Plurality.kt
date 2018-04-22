package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import us.sodiumlabs.electorate.sim.*

open class Plurality : ElectoralSystem {
    companion object {
        val VOTING_STRATEGY = PluralityVotingStrategy()
        val SYSTEM_NAME = ElectoralSystemName("Plurality")
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val ballots = electorate.poll(VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.mapTo(ballotCount) { it.candidate }

        return ballotCount.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .findFirst()
                .orElseThrow { RuntimeException("There should be a candidate with votes!") }
                .element
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    class PluralityVotingStrategy : VotingStrategy<PluralityBallot> {
        override fun accept(voter: Voter, candidates: List<Candidate>): PluralityBallot {
            var maximumUtility = -1.0
            var outCandidate: Candidate? = null

            for(c in candidates) {
                val utility = voter.calculateCandidateUtility(c)

                if(utility > maximumUtility) {
                    maximumUtility = utility
                    outCandidate = c
                }
            }

            if(outCandidate == null) throw RuntimeException("No candidates with positive utility for voter!")

            return PluralityBallot(outCandidate)
        }
    }

    class PluralityBallot(val candidate: Candidate): Ballot
}