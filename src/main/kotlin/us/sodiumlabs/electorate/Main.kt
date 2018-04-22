package us.sodiumlabs.electorate

import com.google.common.collect.ImmutableList
import us.sodiumlabs.electorate.plurality.ElectedTwoPartyPlurality
import us.sodiumlabs.electorate.plurality.Plurality
import us.sodiumlabs.electorate.plurality.StrategicPlurality
import us.sodiumlabs.electorate.plurality.RandomTwoPartyPlurality
import us.sodiumlabs.electorate.range.RangeVoting
import us.sodiumlabs.electorate.sim.ElectionSim
import us.sodiumlabs.electorate.sim.Policy
import us.sodiumlabs.electorate.sim.generateElectorate
import us.sodiumlabs.electorate.sortition.Sortition
import java.util.*

fun main(args: Array<String>) {
    val seedRandom = Random(0x1337_AFC0_FFEE_BEEF)
    val electorateRandom = Random(seedRandom.nextLong())

    val policies = ImmutableList.of(
            Policy("Apples"),
            Policy("Oranges"),
            Policy("Bananas"),
            Policy("Lemons"),
            Policy("Grapes") )

    val electionSim = ElectionSim(ImmutableList.of(
        Sortition(Random(seedRandom.nextLong())),
        Plurality(),
        StrategicPlurality(),
        RandomTwoPartyPlurality(Random(seedRandom.nextLong())),
        ElectedTwoPartyPlurality(),
        RangeVoting()
    ))

    for( i in 1..100 ) {
        val electorate = generateElectorate(electorateRandom, policies, 100, 10)
        electionSim.runElectionSuite(electorate)
    }

    electionSim.printRegret()
}