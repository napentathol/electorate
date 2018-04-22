package us.sodiumlabs.electorate.ranked

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.Multiset
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate

class InstantRunnoff: ElectoralSystem {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Ranked - IRV - Pure")
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val ballots = electorate.poll(PureRankingStrategy.RANKING_STRATEGY)
        val candidates = electorate.candidates.toMutableList()

        var ballotCount = countBallots(ballots, candidates)
        while(candidates.size > 1 && !hasWinner(ballots, ballotCount)) {
            candidates.remove(findLoser(ballotCount))
            ballotCount = countBallots(ballots, candidates)
        }

        return findFirst(ballotCount)
    }

    private fun countBallots(ballots: List<RankedBallot>, candidates: List<Candidate>): Multiset<Candidate> {
        val ballotCount = HashMultiset.create<Candidate>()

        for(b in ballots) {
            candidateLoop@ for(c in b.candidates) {
                if(candidates.contains(c)) {
                    ballotCount.add(c)
                    break@candidateLoop
                }
            }
        }

        return ImmutableMultiset.copyOf(ballotCount)
    }

    private fun hasWinner(ballots: List<RankedBallot>, ballotCount: Multiset<Candidate>): Boolean {
        val currentFirst = findFirst(ballotCount)
        return ballotCount.count(currentFirst) > ballots.size/2
    }

    private fun findLoser(ballotCount: Multiset<Candidate>): Candidate = sortedCandidateList(ballotCount).last()

    override fun getSystemName(): ElectoralSystemName = SYSTEM_NAME
}