package us.sodiumlabs.electorate.approval

import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMap
import us.sodiumlabs.electorate.BigDecimalAverageCollector
import us.sodiumlabs.electorate.generateRandomBigDecimal
import us.sodiumlabs.electorate.sim.Ballot
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import us.sodiumlabs.electorate.sim.Voter
import us.sodiumlabs.electorate.sim.VotingStrategy
import java.math.BigDecimal
import java.util.Optional
import java.util.Random

class ApprovalVoting(private val approvalVotingStrategy: ApprovalVotingStrategy) : ElectoralSystem {

    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(approvalVotingStrategy)
        val ballotCount = HashMultiset.create<Candidate>()

        ballots.flatMap { it.approvalMap.entries }
            .filter { it.value }
            .mapTo(ballotCount) { it.key }

        return findFirst(ballotCount)
    }

    override fun getSystemName(): ElectoralSystemName {
        return approvalVotingStrategy.getName()
    }

    class ApprovalBallot(val approvalMap: Map<Candidate, Boolean>) : Ballot

    abstract class ApprovalVotingStrategy : VotingStrategy<ApprovalBallot> {
        protected fun internalAccept(voter: Voter, candidates: List<Candidate>, threshold: BigDecimal): ApprovalBallot {
            val mapBuilder = ImmutableMap.builder<Candidate, Boolean>()
            for (c in candidates) {
                mapBuilder.put(c, voter.calculateCandidateUtility(c) > threshold)
            }
            return ApprovalBallot(mapBuilder.build())
        }

        abstract fun getName(): ElectoralSystemName
    }

    class ThresholdApprovalVotingStrategy(private val threshold: BigDecimal) : ApprovalVotingStrategy() {
        override fun getName(): ElectoralSystemName = ElectoralSystemName("Approval - Set Threshold (t=$threshold)")

        override fun accept(voter: Voter, candidates: List<Candidate>): ApprovalBallot =
            internalAccept(voter, candidates, threshold)
    }

    class RandomThresholdApprovalVotingStrategy(private val random: Random) : ApprovalVotingStrategy() {
        override fun getName(): ElectoralSystemName = ElectoralSystemName("Approval - Random Threshold")

        override fun accept(voter: Voter, candidates: List<Candidate>): ApprovalBallot =
            internalAccept(voter, candidates, generateRandomBigDecimal(random))
    }

    class MeanThresholdAppovalVotingStrategy : ApprovalVotingStrategy() {
        override fun getName(): ElectoralSystemName = ElectoralSystemName("Approval - Mean Threshold")

        override fun accept(voter: Voter, candidates: List<Candidate>): ApprovalBallot {
            val threshold = candidates.stream()
                .map { voter.calculateCandidateUtility(it) }
                .collect(BigDecimalAverageCollector())

            return internalAccept(voter, candidates, threshold)
        }
    }
}
