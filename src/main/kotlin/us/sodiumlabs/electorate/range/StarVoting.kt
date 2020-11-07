package us.sodiumlabs.electorate.range

import com.google.common.collect.HashMultiset
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.Optional

/**
 * Score-then-runoff
 */
class StarVoting : RangeVoting() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Range - STAR - Pure")
    }

    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        ballots
            .flatMap { it.candidateMarks.entrySet() }
            .forEach { ballotCount.add(it.element, it.count) }

        val candidates = sortedCandidateList(ballotCount)

        ballotCount.clear()

        val candidate1 = candidates[0]
        val candidate2 = candidates[1]

        // In case there is no preference, ensure one of the winners will win.
        ballotCount.add(candidate1)
        ballotCount.add(candidate2)

        ballots.forEach {
            val preference1 = it.candidateMarks.count(candidate1)
            val preference2 = it.candidateMarks.count(candidate2)

            if (preference1 > preference2) {
                ballotCount.add(candidate1)
            } else if (preference2 > preference1) {
                ballotCount.add(candidate2)
            }
        }

        return findFirst(ballotCount)
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }
}
