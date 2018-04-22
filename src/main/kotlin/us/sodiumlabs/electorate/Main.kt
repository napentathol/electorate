package us.sodiumlabs.electorate

import com.google.common.collect.ImmutableList
import us.sodiumlabs.electorate.plurality.Plurality
import us.sodiumlabs.electorate.plurality.StrategicPlurality
import us.sodiumlabs.electorate.plurality.TwoPartyPlurality
import us.sodiumlabs.electorate.sim.ElectionSim
import us.sodiumlabs.electorate.sim.Policy
import us.sodiumlabs.electorate.sim.generateElectorate
import us.sodiumlabs.electorate.sortition.Sortition
import java.util.*

fun main(args: Array<String>) {
    val generatorRandom = Random(0x1337)
    val electoralRandom = Random(0x1337AF)

    val policies = ImmutableList.of(
            Policy("Apples"),
            Policy("Oranges"),
            Policy("Bananas"),
            Policy("Lemons"),
            Policy("Grapes") )

    val electionSim = ElectionSim(ImmutableList.of(
        Sortition(electoralRandom),
        Plurality(),
        StrategicPlurality(),
        TwoPartyPlurality(electoralRandom)
    ))

    for( i in 1..100 ) {
        val electorate = generateElectorate(generatorRandom, policies, 100, 10)
        electionSim.runElectionSuite(electorate)
    }

    electionSim.printRegret()
}