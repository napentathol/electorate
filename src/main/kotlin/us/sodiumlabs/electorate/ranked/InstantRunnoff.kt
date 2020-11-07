package us.sodiumlabs.electorate.ranked

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.Multiset
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.Optional

class InstantRunnoff : ElectoralSystem {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Ranked - IRV - Pure")
    }

    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(PureRankingStrategy.RANKING_STRATEGY)
        val candidates = electorate.candidates.toMutableList()

        var ballotCount = countBallots(ballots, candidates)
        while (candidates.size > 1 && !hasWinner(ballots, ballotCount)) {
            candidates.removeAll(findLosers(ballotCount))
            ballotCount = countBallots(ballots, candidates)
        }

        if (candidates.size == 0) {
            return Optional.empty()
        }

        return findFirst(ballotCount)
    }

    private fun countBallots(ballots: List<RankedBallot>, candidates: List<Candidate>): Multiset<Candidate> {
        val ballotCount = HashMultiset.create<Candidate>()

        for (b in ballots) {
            candidateLoop@ for (c in b.candidates) {
                if (candidates.contains(c)) {
                    ballotCount.add(c)
                    break@candidateLoop
                }
            }
        }

        return ImmutableMultiset.copyOf(ballotCount)
    }

    private fun hasWinner(ballots: List<RankedBallot>, ballotCount: Multiset<Candidate>): Boolean {
        val currentFirst = findFirst(ballotCount)
        return ballotCount.count(currentFirst) > ballots.size / 2
    }

    private fun findLosers(ballotCount: Multiset<Candidate>): List<Candidate> {
        val sortedCandidates = sortedCandidateList(ballotCount)
        val last = sortedCandidates.last()
        val lastCount = ballotCount.count(last)

        return ballotCount.entrySet()
            .filter { it.count == lastCount }
            .map { it.element }
    }

    override fun getSystemName(): ElectoralSystemName = SYSTEM_NAME
}
