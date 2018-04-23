package us.sodiumlabs.electorate.ranked

import com.google.common.collect.ImmutableList
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.Electorate
import us.sodiumlabs.electorate.sim.VotingStrategy
import org.junit.jupiter.api.Assertions.assertEquals

internal class RankedPairsTest {
    companion object {
        val candidateA = Candidate(ImmutableList.of())
        val candidateB = Candidate(ImmutableList.of())
        val candidateC = Candidate(ImmutableList.of())
        val candidateD = Candidate(ImmutableList.of())
    }

    @Test
    fun produceCandidate_happy() {
        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn createBallots(candidateA, candidateB, candidateC, 3)
        }

        val ranked = RankedPairs()

        assertEquals(candidateA, ranked.produceCandidate(electorate))
    }

    @Test
    fun produceCandidate_single() {
        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn createBallots(candidateB, candidateA, candidateC, 1)
        }

        val ranked = RankedPairs()

        assertEquals(candidateB, ranked.produceCandidate(electorate))
    }

    @Test
    fun produceCandidate_many() {
        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn createBallots(candidateC, candidateA, candidateB, 100)
        }

        val ranked = RankedPairs()

        assertEquals(candidateC, ranked.produceCandidate(electorate))
    }

    @Test
    fun produceCandidate_cycle() {
        val list = ImmutableList.builder<RankedBallot>()
                .addAll(createBallots(candidateA, candidateB, candidateC, 4))
                .addAll(createBallots(candidateB, candidateC, candidateA, 3))
                .addAll(createBallots(candidateC, candidateA, candidateB, 3))
                .build()

        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn list
        }

        val ranked = RankedPairs()

        assertEquals(candidateA, ranked.produceCandidate(electorate))
    }

    @Test
    fun produceCandidate_participationIssue_unidealCase() {
        val list = ImmutableList.builder<RankedBallot>()
                .addAll(createBallots(candidateA, candidateB, candidateC, candidateD, 4))
                .addAll(createBallots(candidateA, candidateD, candidateB, candidateC, 8))
                .addAll(createBallots(candidateB, candidateC, candidateA, candidateD, 7))
                .addAll(createBallots(candidateC, candidateD, candidateB, candidateA, 7))
                .build()

        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn list
        }

        val ranked = RankedPairs()

        assertEquals(candidateB, ranked.produceCandidate(electorate))
    }

    @Test
    fun produceCandidate_participationIssue_idealCase() {
        val list = ImmutableList.builder<RankedBallot>()
                .addAll(createBallots(candidateA, candidateD, candidateB, candidateC, 8))
                .addAll(createBallots(candidateB, candidateC, candidateA, candidateD, 7))
                .addAll(createBallots(candidateC, candidateD, candidateB, candidateA, 7))
                .build()

        val electorate = mock<Electorate> {
            on { poll(any<VotingStrategy<RankedBallot>>()) } doReturn list
        }

        val ranked = RankedPairs()

        assertEquals(candidateA, ranked.produceCandidate(electorate))
    }

    private fun createBallots(a: Candidate, b: Candidate, c: Candidate, count: Int): List<RankedBallot> {
        val listBuilder = ImmutableList.builder<RankedBallot>()
        for(i in 0 until count) {
            listBuilder.add(createBallot(a, b, c))
        }
        return listBuilder.build()
    }

    private fun createBallots(a: Candidate, b: Candidate, c: Candidate, d: Candidate, count: Int): List<RankedBallot> {
        val listBuilder = ImmutableList.builder<RankedBallot>()
        for(i in 0 until count) {
            listBuilder.add(createBallot(a, b, c, d))
        }
        return listBuilder.build()
    }

    private fun createBallot(a: Candidate, b: Candidate, c: Candidate): RankedBallot = RankedBallot(ImmutableList.of(a,b,c))
    private fun createBallot(a: Candidate, b: Candidate, c: Candidate, d: Candidate): RankedBallot = RankedBallot(ImmutableList.of(a,b,c,d))
}