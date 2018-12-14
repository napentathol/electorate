package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals
import java.math.BigDecimal

internal class ElectorateTest {
    companion object {
        val ONE: BigDecimal = BigDecimal("1.0000000000")
        val ZERO: BigDecimal = BigDecimal("0E-10")
    }

    @Test
    fun calculateRegret_baseCase() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(ONE, electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_singleCandidate() {
        val candidate1 = createCandidate(0.0, 0.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1))

        assertEquals(ZERO, electorate.calculateRegret(candidate1))
    }

    @Test
    fun calculateRegret_singleVoter() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1), ImmutableList.of(candidate1, candidate2))

        assertEquals(ONE, electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_allCandidateEqual_differentCandidatePositions() {
        val candidate1 = createCandidate(1.0, 0.0)
        val candidate2 = createCandidate(0.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(ZERO, electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_allCandidateEqual_differentVoterPositions() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.0, 1.0)
        val voter2 = createVoter(1.0, 0.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(ZERO, electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryCandidatePositions() {
        val candidate1 = createCandidate(0.5, 0.5)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(BigDecimal("0.5000000000"), electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryVoterPositions() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.5, 1.0)
        val voter2 = createVoter(1.0, 0.5)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(BigDecimal("0.5000000000"), electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryPositions() {
        val candidate1 = createCandidate(0.5, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.5, 1.0)
        val voter2 = createVoter(1.0, 0.5)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(BigDecimal("0.2500000000"), electorate.calculateRegret(candidate1))
        assertEquals(ZERO, electorate.calculateRegret(candidate2))
    }

    @Test
    fun generateCandidatesFromVoters_allRunning() {
        val voter1 = createVoter(0.0, 1.0, 1.0)
        val voter2 = createVoter(1.0, 0.0, 0.99)
        val voter3 = createVoter(0.5, 0.5, 0.98)

        val voters = ImmutableList.of(voter1, voter2, voter3)

        val candidates = generateCandidatesFromVoters(voters, 3)

        assertEquals(3, candidates.size)
        assertEquals(Candidate(voter1), candidates[0])
        assertEquals(Candidate(voter2), candidates[1])
        assertEquals(Candidate(voter3), candidates[2])
    }

    @Test
    fun generateCandidatesFromVoters_oneNotRunningLimited() {
        val voter1 = createVoter(0.0, 1.0, 1.0)
        val voter2 = createVoter(1.0, 0.0, 0.99)
        val voter3 = createVoter(0.5, 0.5, 0.98)

        val voters = ImmutableList.of(voter1, voter2, voter3)

        val candidates = generateCandidatesFromVoters(voters, 2)

        assertEquals(2, candidates.size)
        assertEquals(Candidate(voter1), candidates[0])
        assertEquals(Candidate(voter2), candidates[1])
    }

    @Test
    fun generateCandidatesFromVoters_oneNotRunningSatisfiedWithCandidate() {
        val voter1 = createVoter(0.0, 1.0, 1.0)
        val voter2 = createVoter(1.0, 0.0, 0.99)
        val voter3 = createVoter(0.0, 1.0, 0.98)

        val voters = ImmutableList.of(voter1, voter2, voter3)

        val candidates = generateCandidatesFromVoters(voters, 3)

        assertEquals(2, candidates.size)
        assertEquals(Candidate(voter1), candidates[0])
        assertEquals(Candidate(voter2), candidates[1])
    }

    private fun createVoter(issue1: Double, issue2: Double): Voter = createVoter(issue1, issue2, 0.0)

    private fun createVoter(issue1: Double, issue2: Double, runningPotential: Double): Voter {
        return Voter(ImmutableList.of(
                Stance(Policy("Issue1"), BigDecimal.valueOf(issue1)),
                Stance(Policy("Issue2"), BigDecimal.valueOf(issue2))),
                BigDecimal(runningPotential))
    }

    private fun createCandidate(issue1: Double, issue2: Double): Candidate {
        return Candidate(ImmutableList.of(
                Stance(Policy("Issue1"), BigDecimal.valueOf(issue1)),
                Stance(Policy("Issue2"), BigDecimal.valueOf(issue2))))
    }
}