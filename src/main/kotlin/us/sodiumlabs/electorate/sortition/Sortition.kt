package us.sodiumlabs.electorate.sortition

import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.*

open class Sortition(private val random: Random): ElectoralSystem {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Sortition")
    }

    override fun produceCandidate(electorate: Electorate): Candidate {
        return electorate.candidates[random.nextInt(electorate.candidates.size)]
    }

    override fun getSystemName(): ElectoralSystemName {
        return SYSTEM_NAME
    }

}