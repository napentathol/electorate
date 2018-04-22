package us.sodiumlabs.electorate.range

import com.google.common.collect.HashMultiset
import com.google.common.collect.Multiset
import us.sodiumlabs.electorate.sim.*
import java.math.BigDecimal

open class RangeVoting : ElectoralSystem {
    companion object {
        val VOTING_STRATEGY = RangeVotingStrategy()
        val SYSTEM_NAME = ElectoralSystemName("Range Voting")
        val MAX_VOTE: BigDecimal = BigDecimal.valueOf(5)
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val ballots = electorate.poll(VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        ballots
                .flatMap { it.candidateMarks.entrySet() }
                .forEach { ballotCount.add(it.element, it.count) }

        return findFirst(ballotCount)
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    class RangeBallot(val candidateMarks: Multiset<Candidate>): Ballot {
        init {
            for (e in candidateMarks.entrySet()) {
                check(e.count > 0, {"Count must be greater than 0, was ${e.count}"})
                check(e.count <= MAX_VOTE.toInt(), {"Count must be less than 5, was ${e.count}"})
            }
        }
    }

    class RangeVotingStrategy: VotingStrategy<RangeBallot> {
        override fun accept(voter: Voter, candidates: List<Candidate>): RangeBallot {
            val marks = HashMultiset.create<Candidate>()

            candidates.forEach {
                marks.add(it, (voter.calculateCandidateUtility(it) * (MAX_VOTE + BigDecimal.ONE)).toInt())
            }

            return RangeBallot(marks)
        }
    }
}
