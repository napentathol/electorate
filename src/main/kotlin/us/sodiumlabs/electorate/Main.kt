package us.sodiumlabs.electorate

import com.google.common.collect.ImmutableList
import us.sodiumlabs.electorate.approval.ApprovalVoting
import us.sodiumlabs.electorate.plurality.ElectedTwoPartyPlurality
import us.sodiumlabs.electorate.plurality.Plurality
import us.sodiumlabs.electorate.plurality.RandomTwoPartyPlurality
import us.sodiumlabs.electorate.plurality.StrategicPlurality
import us.sodiumlabs.electorate.range.RangeVoting
import us.sodiumlabs.electorate.range.StarVoting
import us.sodiumlabs.electorate.ranked.InstantRunnoff
import us.sodiumlabs.electorate.ranked.RankedPairs
import us.sodiumlabs.electorate.sim.ElectionSim
import us.sodiumlabs.electorate.sim.Policy
import us.sodiumlabs.electorate.sim.generateElectorate
import us.sodiumlabs.electorate.sim.wrap
import us.sodiumlabs.electorate.sortition.Sortition
import java.math.BigDecimal
import java.util.Random

fun main() {
    val seedRandom = Random(0x1337_AFC0_FFEE_BEEF)
    val electorateRandom = Random(seedRandom.nextLong())

    val policies = ImmutableList.of(
        Policy("Apples"),
        Policy("Oranges"),
        Policy("Bananas"),
        Policy("Lemons"),
        Policy("Grapes")
    )

    val electionSim = ElectionSim(
        ImmutableList.of(
            // Random elections
            Sortition(Random(seedRandom.nextLong())),
            // Plurality elections
            Plurality(),
            StrategicPlurality(),
            RandomTwoPartyPlurality(Random(seedRandom.nextLong())),
            ElectedTwoPartyPlurality(),
            // Range elections
            RangeVoting(),
            StarVoting(),
            // Ranked elections
            InstantRunnoff(),
            RankedPairs(),
            // Approval elections
            ApprovalVoting(ApprovalVoting.ThresholdApprovalVotingStrategy(wrap(BigDecimal.valueOf(0.5)))),
            ApprovalVoting(ApprovalVoting.ThresholdApprovalVotingStrategy(wrap(BigDecimal.valueOf(0.75)))),
            ApprovalVoting(ApprovalVoting.ThresholdApprovalVotingStrategy(wrap(BigDecimal.valueOf(0.25)))),
            ApprovalVoting(ApprovalVoting.MeanThresholdAppovalVotingStrategy()),
            ApprovalVoting(ApprovalVoting.RandomThresholdApprovalVotingStrategy(Random(seedRandom.nextLong())))
        )
    )

    for (i in 1..100) {
        val electorate = generateElectorate(electorateRandom, policies, 1000, 10)
        electionSim.runElectionSuite(electorate)
    }

    electionSim.printRegret()
}
