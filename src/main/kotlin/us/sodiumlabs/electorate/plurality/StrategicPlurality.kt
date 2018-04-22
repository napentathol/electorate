package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate

open class StrategicPlurality : Plurality() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Plurality - Strategic")
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val preBallots = electorate.poll(Plurality.VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        preBallots.mapTo(ballotCount) { it.candidate }

        val ranking = sortedCandidateList(ballotCount)

        ballotCount.clear()

        val ballots = electorate.poll(Plurality.VOTING_STRATEGY, ranking.subList(0,2))

        ballots.mapTo(ballotCount) { it.candidate }

        return findFirst(ballotCount)
    }
}