// port-lint: source lib.rs
package io.github.kotlinmania.mime

/**
 * A section of a [Mime].
 *
 * For instance, for the Mime `image/svg+xml`, it contains 3 [Name]s,
 * `image`, `svg`, and `xml`.
 *
 * In most cases, [Name]s are compared ignoring case.
 */
// Future optimization for Name (see the `source` field below): optimize with
// an Atom-like thing. There a `val` Names, and so it is possible for the
// statis strings to havea different memory address. Additionally, when used
// in when expressions, the strings are compared with a byte-wise comparison,
// possibly even if the address and length are the same.
//
// Being an enum with an Atom variant that is an Int (and without a string
// pointer and boolean) would allow for faster comparisons.
class Name internal constructor(
    internal val source: String,
    internal val insensitive: Boolean,
) : Comparable<Name> {
    /**
     * Get the value of this [Name] as a string.
     *
     * Note that the borrow is not tied to `this` but the underlying string,
     * allowing the string to outlive [Name]. Alternately, there is a
     * `Name.toString()` override that returns the source string directly.
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

// Name ============

internal fun nameEqStr(name: Name, s: String): Boolean =
    if (name.insensitive) eqAscii(name.source, s) else name.source == s
