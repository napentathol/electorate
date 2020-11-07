package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableList
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.Optional
import java.util.Random

open class RandomTwoPartyPlurality(private val random: Random) : Plurality() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Plurality - Random Two Party")
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(VOTING_STRATEGY, getRandomCandidates(electorate))
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.mapTo(ballotCount) { it.candidate }

        return findFirst(ballotCount)
    }

    private fun getRandomCandidates(electorate: Electorate): List<Candidate> {
        return ImmutableList.of(getRandomCandidate(electorate), getRandomCandidate(electorate))
    }

    private fun getRandomCandidate(electorate: Electorate): Candidate {
        return electorate.candidates[random.nextInt(electorate.candidates.size)]
    }
}
