// port-lint: source lib.rs
package io.github.kotlinmania.mime

/**
 * An error when parsing a [Mime] from a string.
 */
class FromStrError internal constructor(internal val inner: ParseError) :
    Exception("mime parse error: $inner") {

    internal fun s(): String = "mime parse error"

    override fun toString(): String = "${s()}: $inner"
}
