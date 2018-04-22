package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableList
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.*

open class TwoPartyPlurality(val random: Random): Plurality() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Two Party Plurality")
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val ballots = electorate.poll(VOTING_STRATEGY, getRandomCandidates(electorate))
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.mapTo(ballotCount) { it.candidate }

        return ballotCount.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .findFirst()
                .orElseThrow { RuntimeException("There should be a candidate with votes!") }
                .element
    }

    private fun getRandomCandidates(electorate: Electorate): List<Candidate> {
        return ImmutableList.of(getRandomCandidate(electorate), getRandomCandidate(electorate))
    }

    private fun getRandomCandidate(electorate: Electorate): Candidate {
        return electorate.candidates[random.nextInt(electorate.candidates.size)]
    }
}