// port-lint: source lib.rs
package io.github.kotlinmania.mime

// # Mime
//
// Mime is now Media Type, technically, but `Mime` is more immediately
// understandable, so the main type here is `Mime`.
//
// ## What is Mime?
//
// Example mime string: `text/plain`
//
// ```
// val plainText: Mime = fromStr("text/plain")
// assertEquals(plainText, TEXT_PLAIN)
// ```
//
// ## Inspecting Mimes
//
// ```
// val mime = TEXT_PLAIN
// when {
//     mime.type() == TEXT && mime.subtype() == PLAIN -> println("plain text!")
//     mime.type() == TEXT -> println("structured text")
//     else -> println("not text")
// }
// ```

/**
 * A parsed mime or media type.
 */
class Mime internal constructor(
    internal val source: Source,
    internal val slash: Int,
    internal val plus: Int?,
    internal val params: ParamSource,
) : Comparable<Mime> {

    /**
     * Get the top level media type for this [Mime].
     *
     * # Example
     *
     * ```
     * val mime = TEXT_PLAIN
     * assertEquals(mime.type(), "text")
     * assertEquals(mime.type(), TEXT)
     * ```
     */
    fun type(): Name = Name(
        source = source.asRef().substring(0, slash),
        insensitive = true,
    )

    /**
     * Get the subtype of this [Mime].
     *
     * # Example
     *
     * ```
     * val mime = TEXT_PLAIN
     * assertEquals(mime.subtype(), "plain")
     * assertEquals(mime.subtype(), PLAIN)
     * ```
     */
    fun subtype(): Name {
        val end = plus ?: (semicolon() ?: source.asRef().length)
        return Name(
            source = source.asRef().substring(slash + 1, end),
            insensitive = true,
        )
    }

    /**
     * Get an optional +suffix for this [Mime].
     *
     * # Example
     *
     * ```
     * val svg = fromStr("image/svg+xml")
     * assertEquals(svg.suffix(), XML)
     * assertEquals(svg.suffix()!!, "xml")
     *
     *
     * assertTrue(TEXT_PLAIN.suffix() == null)
     * ```
     */
    fun suffix(): Name? {
        val end = semicolon() ?: source.asRef().length
        return plus?.let { idx ->
            Name(
                source = source.asRef().substring(idx + 1, end),
                insensitive = true,
            )
        }
    }

    /**
     * Look up a parameter by name.
     *
     * # Example
     *
     * ```
     * val mime = TEXT_PLAIN_UTF_8
     * assertEquals(mime.getParam(CHARSET), UTF_8)
     * assertEquals(mime.getParam("charset")!!, "utf-8")
     * assertTrue(mime.getParam("boundary") == null)
     *
     * val mime2 = fromStr("multipart/form-data; boundary=ABCDEFG")
     * assertEquals(mime2.getParam(BOUNDARY)!!, "ABCDEFG")
     * ```
     */
    fun getParam(attr: Name): Name? = params().asSequence().firstOrNull { attr == it.first }?.second

    /**
     * Look up a parameter by name string.
     */
    fun getParam(attr: String): Name? = params().asSequence().firstOrNull { nameEqStr(it.first, attr) }?.second

    /**
     * Returns an iterator over the parameters.
     */
    fun params(): Params {
        val inner: ParamsInner = when (val p = params) {
            is ParamSource.Utf8 -> ParamsInner.Utf8
            is ParamSource.Custom -> ParamsInner.Custom(source, p.params.iterator())
            is ParamSource.None -> ParamsInner.None
        }
        return Params(inner)
    }

    /**
     * Return a `String` of the [Mime]'s ["essence"][essence].
     *
     * [essence]: https://mimesniff.spec.whatwg.org/#mime-type-essence
     */
    fun essenceStr(): String {
        val end = semicolon() ?: source.asRef().length
        return source.asRef().substring(0, end)
    }

    internal fun hasParams(): Boolean = params !is ParamSource.None

    internal fun semicolon(): Int? = when (val p = params) {
        is ParamSource.Utf8 -> p.semicolon
        is ParamSource.Custom -> p.semicolon
        is ParamSource.None -> null
    }

    internal fun atom(): Int = when (val s = source) {
        is Source.Atom -> s.tag
        else -> 0
    }

    fun asStr(): String = source.asRef()

    // Future optimization for the Mime equals override below:
    // This could optimize for when there are no customs parameters.
    // Any parsed mime has already been lowercased, so if there aren't
    // any parameters that are case sensistive, this can skip the
    // eqAscii, and just use a byte-wise comparison instead.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return when (other) {
            is Mime -> {
                val a = this.atom()
                val b = other.atom()
                if (a == 0 || b == 0) mimeEqStr(this, other.source.asRef())
                else a == b
            }
            is String -> mimeEqStr(this, other)
            else -> false
        }
    }

    override fun hashCode(): Int = source.asRef().lowercase().hashCode()

    override fun compareTo(other: Mime): Int = source.asRef().compareTo(other.source.asRef())

    override fun toString(): String = source.asRef()
}

// Mime ============

internal fun eqAscii(a: String, b: String): Boolean = a.equals(b, ignoreCase = true)

internal fun mimeEqStr(mime: Mime, s: String): Boolean {
    val p = mime.params
    return when {
        p is ParamSource.Utf8 -> {
            if (mime.source.asRef().length == s.length) eqAscii(mime.source.asRef(), s)
            else paramsEq(p.semicolon, mime.source.asRef(), s)
        }
        mime.semicolon() != null -> paramsEq(mime.semicolon()!!, mime.source.asRef(), s)
        else -> eqAscii(mime.source.asRef(), s)
    }
}

private fun paramsEq(semicolon: Int, a: String, b: String): Boolean {
    if (b.length < semicolon + 1) return false
    if (!eqAscii(a.substring(0, semicolon), b.substring(0, semicolon))) return false

    // gotta check for quotes, LWS, and for case senstive names
    var aRest = a.substring(semicolon + 1)
    var bRest = b.substring(semicolon + 1)
    var sensitive: Boolean

    while (true) {
        aRest = aRest.trim()
        bRest = bRest.trim()

        val aEmpty = aRest.isEmpty()
        val bEmpty = bRest.isEmpty()
        when {
            aEmpty && bEmpty -> return true
            aEmpty || bEmpty -> return false
        }

        //name
        val aIdx = aRest.indexOf('=')
        if (aIdx < 0) return false
        val aName = aRest.substring(0, aIdx).trimStart()
        val bIdx = bRest.indexOf('=')
        if (bIdx < 0) return false
        val bName = bRest.substring(0, bIdx).trimStart()
        if (!eqAscii(aName, bName)) return false
        sensitive = !nameEqStr(CHARSET, aName)
        aRest = aRest.substring(0, aIdx)
        bRest = bRest.substring(0, bIdx)

        //value
        val aQuoted = if (aRest.isNotEmpty() && aRest[0] == '"') {
            aRest = aRest.substring(1)
            true
        } else false
        val bQuoted = if (bRest.isNotEmpty() && bRest[0] == '"') {
            bRest = bRest.substring(1)
            true
        } else false

        val aEnd = if (aQuoted) {
            val q = aRest.indexOf('"')
            if (q < 0) return false
            q
        } else {
            val sc = aRest.indexOf(';')
            if (sc < 0) aRest.length else sc
        }

        val bEnd = if (bQuoted) {
            val q = bRest.indexOf('"')
            if (q < 0) return false
            q
        } else {
            val sc = bRest.indexOf(';')
            if (sc < 0) bRest.length else sc
        }

        if (sensitive) {
            if (!eqAscii(aRest.substring(0, aEnd), bRest.substring(0, bEnd))) return false
        } else {
            if (aRest.substring(0, aEnd) != bRest.substring(0, bEnd)) return false
        }
        aRest = aRest.substring(aEnd)
        bRest = bRest.substring(bEnd)
    }
}

internal fun fromStr(s: String): Mime {
    try {
        return parse(s)
    } catch (e: ParseError) {
        throw FromStrError(e)
    }
}
