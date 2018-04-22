package us.sodiumlabs.electorate.sim

import com.google.common.collect.ImmutableList
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ElectorateTest {

    @Test
    fun calculateRegret_baseCase() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(1.0, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_singleCandidate() {
        val candidate1 = createCandidate(0.0, 0.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1))

        assertEquals(0.0, electorate.calculateRegret(candidate1))
    }

    @Test
    fun calculateRegret_singleVoter() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1), ImmutableList.of(candidate1, candidate2))

        assertEquals(1.0, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_allCandidateEqual_differentCandidatePositions() {
        val candidate1 = createCandidate(1.0, 0.0)
        val candidate2 = createCandidate(0.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(0.0, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_allCandidateEqual_differentVoterPositions() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.0, 1.0)
        val voter2 = createVoter(1.0, 0.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(0.0, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryCandidatePositions() {
        val candidate1 = createCandidate(0.5, 0.5)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(1.0, 1.0)
        val voter2 = createVoter(1.0, 1.0)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(0.5, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryVoterPositions() {
        val candidate1 = createCandidate(0.0, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.5, 1.0)
        val voter2 = createVoter(1.0, 0.5)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(0.5, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    @Test
    fun calculateRegret_nonBinaryPositions() {
        val candidate1 = createCandidate(0.5, 0.0)
        val candidate2 = createCandidate(1.0, 1.0)

        val voter1 = createVoter(0.5, 1.0)
        val voter2 = createVoter(1.0, 0.5)

        val electorate = Electorate(ImmutableList.of(voter1, voter2), ImmutableList.of(candidate1, candidate2))

        assertEquals(0.25, electorate.calculateRegret(candidate1))
        assertEquals(0.0, electorate.calculateRegret(candidate2))
    }

    private fun createVoter(issue1: Double, issue2: Double): Voter {
        return Voter(ImmutableList.of(
                Stance(Policy("Issue1"), issue1),
                Stance(Policy("Issue2"), issue2)))
    }

    private fun createCandidate(issue1: Double, issue2: Double): Candidate {
        return Candidate(ImmutableList.of(
                Stance(Policy("Issue1"), issue1),
                Stance(Policy("Issue2"), issue2)))
    }

}