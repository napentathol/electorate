package us.sodiumlabs.electorate.ranked

import com.google.common.collect.HashBasedTable
import us.sodiumlabs.electorate.sim.Candidate
import us.sodiumlabs.electorate.sim.ElectoralSystem
import us.sodiumlabs.electorate.sim.ElectoralSystemName
import us.sodiumlabs.electorate.sim.Electorate
import java.util.Optional

class RankedPairs : ElectoralSystem {
    companion object {
        val SYSTEM_NAME = ElectoralSystemName("Ranked - Ranked Pairs - Pure")
    }

    override fun electCandidate(electorate: Electorate): Optional<Candidate> {
        val ballots = electorate.poll(PureRankingStrategy.RANKING_STRATEGY)
        val candidateTable = HashBasedTable.create<Candidate, Candidate, Int>()

        // Tally
        ballots.forEach {
            for (i in 0 until it.candidates.size - 1) {
                for (j in i + 1 until it.candidates.size) {
                    val c1 = it.candidates[i]
                    val c2 = it.candidates[j]
                    val n = Optional.ofNullable(candidateTable.get(c1, c2)).orElse(0)
                    candidateTable.put(c1, c2, n + 1)
                }
            }
        }

        val rankedPairGraph = RankedPairGraph()

        // Sort
        candidateTable.cellSet()
            .sortedBy { it.value }
            .reversed()
            // Lock
            .forEach { rankedPairGraph.add(it.rowKey, it.columnKey, it.value) }

        return rankedPairGraph.findHeadCandidate()
    }

    override fun getSystemName(): ElectoralSystemName = SYSTEM_NAME

    class RankedPairGraph {
        private val rankedPairGraphNodes = ArrayList<RankedPairGraphNode>()

        fun add(a: Candidate?, b: Candidate?, weight: Int?) {
            checkNotNull(a)
            checkNotNull(b)
            checkNotNull(weight)

            val nodeA = findNode(a).orElseGet {
                val node = RankedPairGraphNode(a)
                rankedPairGraphNodes.add(node)
                node
            }
            val nodeB = findNode(b).orElseGet {
                val node = RankedPairGraphNode(b)
                rankedPairGraphNodes.add(node)
                node
            }

            nodeA.addVertex(RankedPairGraphVertex(nodeA, nodeB))
        }

        fun findHeadCandidate(): Optional<Candidate> = Optional.of(rankedPairGraphNodes.first().findHeadNode().value)

        private fun findNode(candidate: Candidate): Optional<RankedPairGraphNode> {
            return rankedPairGraphNodes
                .firstOrNull { it.value == candidate }
                ?.let { Optional.of(it) }
                ?: Optional.empty()
        }
    }

    class RankedPairGraphNode(val value: Candidate) {
        private val vertices = ArrayList<RankedPairGraphVertex>()

        fun addVertex(vertex: RankedPairGraphVertex) {
            if (vertex.a == this && !upstreamContains(vertex.b)) {
                vertices.add(vertex)
                vertex.b.addVertex(vertex)
            } else if (vertex.b == this && !downstreamContains(vertex.a)) {
                vertices.add(vertex)
            }
        }

        fun findHeadNode(): RankedPairGraphNode {
            return vertices.firstOrNull { it.b == this }?.a?.findHeadNode() ?: this
        }

        private fun downstreamContains(node: RankedPairGraphNode): Boolean {
            return vertices.any {
                it.b == node || it.a == this && it.b.downstreamContains(node)
            }
        }

        private fun upstreamContains(node: RankedPairGraphNode): Boolean {
            return vertices.any {
                it.a == node || it.b == this && it.a.upstreamContains(node)
            }
        }
    }

    class RankedPairGraphVertex(val a: RankedPairGraphNode, val b: RankedPairGraphNode)
}
