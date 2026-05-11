// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

/**
 * A section of a [Mime].
 *
 * For instance, for the Mime `image/svg+xml`, it contains 3 [Name]s,
 * `image`, `svg`, and `xml`.
 *
 * In most cases, [Name]s are compared ignoring case.
 */
class Name internal constructor(
    internal val source: String,
    internal val insensitive: Boolean,
) : Comparable<Name> {

    /**
     * Get the value of this [Name] as a string.
     */
    fun asStr(): String = source

    override fun toString(): String = source

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is Name -> nameEqStr(this, other.source)
            is String -> nameEqStr(this, other)
            else -> false
        }
    }

    override fun hashCode(): Int =
        if (insensitive) source.lowercase().hashCode() else source.hashCode()

    override fun compareTo(other: Name): Int = source.compareTo(other.source)
}

internal fun nameEqStr(name: Name, s: String): Boolean =
    if (name.insensitive) eqAscii(name.source, s) else name.source == s

infix fun Name.equalsString(other: String): Boolean = nameEqStr(this, other)
infix fun String.equalsName(other: Name): Boolean = nameEqStr(other, this)
