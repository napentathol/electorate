package us.sodiumlabs.electorate.ranked

import com.google.common.collect.HashBasedTable
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import us.sodiumlabs.electorate.sim.VotingStrategy
import java.util.Optional

class RankedApproval(private val strategy: RankedApprovalStrategy) : ElectoralSystem {
    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(strategy)
        val candidateTable = HashBasedTable.create<Candidate, Candidate, Int>()
        var candidates = electorate.candidates

        // Tally
        ballots.forEach {
            val missingCandidates = candidates - it.candidates
            for (i in 0 until it.candidates.size - 1) {
                val c1 = it.candidates[i]
                for (j in i + 1 until it.candidates.size) {
                    val c2 = it.candidates[j]
                    val n = Optional.ofNullable(candidateTable.get(c1, c2)).orElse(0)
                    candidateTable.put(c1, c2, n + 1)
                }
                for(c2 in missingCandidates) {

                }
            }
        }

        var condorcetWinner = Optional.empty<Candidate>()
        while(condorcetWinner.isEmpty) {
            // find the condorcet winner and return if it is present
            condorcetWinner = findCondorcetWinner(candidates, candidateTable)
            if(condorcetWinner.isPresent) break

            // remove the least approved candidate(s);
            candidates = removeLeastApprovedCandidates(candidates, candidateTable)
            if(candidates.isEmpty()) return Optional.empty()
        }
        return condorcetWinner
    }

    override fun getSystemName(): ElectoralSystemName = strategy.getSystemName()
}

interface RankedApprovalStrategy : VotingStrategy<RankedBallot> {
    fun getSystemName(): ElectoralSystemName
}
