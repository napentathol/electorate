package us.sodiumlabs.electorate.plurality

import com.google.common.collect.HashMultiset
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import kotlin.streams.toList

open class StrategicPlurality : Plurality() {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Strategic Plurality")
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        val preBallots = electorate.poll(Plurality.VOTING_STRATEGY)
        val ballotCount = HashMultiset.create<Candidate>()

        preBallots.mapTo(ballotCount) { it.candidate }

        val ranking = ballotCount.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .map { it.element }
                .toList()

        ballotCount.clear()

        val ballots = electorate.poll(Plurality.VOTING_STRATEGY, ranking.subList(0,2))

        ballots.mapTo(ballotCount) { it.candidate }

        return ballotCount.entrySet().stream()
                .sorted { o1, o2 -> o2.count - o1.count }
                .findFirst()
                .orElseThrow { RuntimeException("There should be a candidate with votes!") }
                .element
    }
}