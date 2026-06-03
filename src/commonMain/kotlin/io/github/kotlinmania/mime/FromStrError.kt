// port-lint: source lib.rs
package io.github.kotlinmania.mime

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/**
 * An error when parsing a [Mime] from a string.
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
class FromStrError internal constructor(
    internal val inner: ParseError,
) : Exception("mime parse error: $inner") {
    internal fun s(): String = "mime parse error"

    override fun toString(): String = "${s()}: $inner"

    /** Minimum Rust is 1.15, Error::description was still required then */
    fun description(): String = s()
}
