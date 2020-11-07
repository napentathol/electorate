package us.sodiumlabs.electorate.ranked

import us.sodiumlabs.electorate.Tuple
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.Voter
import us.sodiumlabs.electorate.sim.VotingStrategy

class PureRankingStrategy : VotingStrategy<RankedBallot> {
    companion object {
        val RANKING_STRATEGY = PureRankingStrategy()
    }
    override fun accept(voter: Voter, candidates: List<Candidate>): RankedBallot {
        val outList = candidates.map { Tuple(it, voter.calculateCandidateUtility(it)) }
            .sortedBy { it.t }
            .reversed()
            .map { it.s }
            .toList()

        return RankedBallot(outList)
    }
}
