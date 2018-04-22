package us.sodiumlabs.electorate

abstract class StringWrapper(private val s: String) {
    override fun hashCode(): Int {
        return s.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is StringWrapper
                && other.s == s
    }

    override fun toString(): String {
        return s
    }
}