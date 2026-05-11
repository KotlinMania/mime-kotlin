// port-lint: source src/parse.rs
package io.github.kotlinmania.mime

sealed class ParseError : Exception {
    constructor() : super()
    constructor(message: String) : super(message)

    object MissingSlash : ParseError()
    object MissingEqual : ParseError()
    object MissingQuote : ParseError()
    data class InvalidToken(val pos: Int, val byte: Int) : ParseError()

    internal fun s(): String = when (this) {
        is MissingSlash -> "a slash (/) was missing between the type and subtype"
        is MissingEqual -> "an equals sign (=) was missing between a parameter and its value"
        is MissingQuote -> "a quote (\") was missing from a parameter value"
        is InvalidToken -> "an invalid token was encountered"
    }

    override fun toString(): String = when (this) {
        is InvalidToken -> "${s()}, ${byte.toString(16).uppercase()} at position $pos"
        else -> s()
    }

    /** Minimum Rust is 1.15, Error::description was still required then */
    fun description(): String = s()
}

fun parse(s: String): Mime {
    if (s == "*/*") {
        return STAR_STAR
    }

    val bytes = s.encodeToByteArray()
    val len = bytes.size
    var i = 0

    var start: Int
    val slash: Int
    // toplevel
    while (true) {
        if (i >= len) {
            // EOF and no toplevel is no Mime
            throw ParseError.MissingSlash
        }
        val c = bytes[i].toInt() and 0xff
        when {
            isToken(c) -> { i++ }
            c == '/'.code && i > 0 -> {
                slash = i
                start = i + 1
                i++
                break
            }
            else -> throw ParseError.InvalidToken(i, c)
        }
    }

    // sublevel
    var plus: Int? = null
    while (true) {
        if (i >= len) {
            return Mime(
                source = Source.Dynamic(s.lowercase()),
                slash = slash,
                plus = plus,
                params = ParamSource.None,
            )
        }
        val c = bytes[i].toInt() and 0xff
        when {
            c == '+'.code && i > start -> { plus = i; i++ }
            c == ';'.code && i > start -> { start = i; i++; break }
            isToken(c) -> { i++ }
            else -> throw ParseError.InvalidToken(i, c)
        }
    }

    // params
    val params = paramsFromStr(s, bytes, i, start)

    val src = when (params) {
        is ParamSource.Utf8 -> s.lowercase()
        is ParamSource.Custom -> lowerAsciiWithParams(s, params.semicolon, params.params)
        is ParamSource.None -> {
            // Chop off the empty list
            s.substring(0, start).lowercase()
        }
    }

    return Mime(
        source = Source.Dynamic(src),
        slash = slash,
        plus = plus,
        params = params,
    )
}

private fun paramsFromStr(
    s: String,
    bytes: ByteArray,
    iterStart: Int,
    initialStart: Int,
): ParamSource {
    val semicolon = initialStart
    var start = initialStart + 1
    var params: ParamSource = ParamSource.None
    var i = iterStart
    val len = bytes.size

    paramsLoop@ while (start < s.length) {
        val name: Indexed

        // name
        nameLoop@ while (true) {
            if (i >= len) {
                throw ParseError.MissingEqual
            }
            val c = bytes[i].toInt() and 0xff
            when {
                c == ' '.code && i == start -> { start = i + 1; i++; continue@paramsLoop }
                isToken(c) -> { i++ }
                c == '='.code && i > start -> {
                    name = Indexed(start, i)
                    start = i + 1
                    i++
                    break@nameLoop
                }
                else -> throw ParseError.InvalidToken(i, c)
            }
        }

        val value: Indexed
        // values must be restrict-name-char or "anything goes"
        var isQuoted = false

        valueLoop@ while (true) {
            if (isQuoted) {
                if (i >= len) throw ParseError.MissingQuote
                val c = bytes[i].toInt() and 0xff
                when {
                    c == '"'.code && i > start -> {
                        value = Indexed(start, i)
                        i++
                        break@valueLoop
                    }
                    isRestrictedQuotedChar(c) -> { i++ }
                    else -> throw ParseError.InvalidToken(i, c)
                }
            } else {
                if (i >= len) {
                    value = Indexed(start, s.length)
                    start = s.length
                    break@valueLoop
                }
                val c = bytes[i].toInt() and 0xff
                when {
                    c == '"'.code && i == start -> {
                        isQuoted = true
                        start = i + 1
                        i++
                    }
                    isToken(c) -> { i++ }
                    c == ';'.code && i > start -> {
                        value = Indexed(start, i)
                        start = i + 1
                        i++
                        break@valueLoop
                    }
                    else -> throw ParseError.InvalidToken(i, c)
                }
            }
        }

        if (isQuoted) {
            wsLoop@ while (true) {
                if (i >= len) {
                    // eof
                    start = s.length
                    break@wsLoop
                }
                val c = bytes[i].toInt() and 0xff
                when (c) {
                    ';'.code -> {
                        // next param
                        start = i + 1
                        i++
                        break@wsLoop
                    }
                    ' '.code -> {
                        // skip whitespace
                        i++
                    }
                    else -> throw ParseError.InvalidToken(i, c)
                }
            }
        }

        when (val p = params) {
            is ParamSource.Utf8 -> {
                val base = p.semicolon + 2
                val charset = Indexed(base, "charset".length + base)
                val utf8 = Indexed(charset.second + 1, charset.second + "utf-8".length + 1)
                params = ParamSource.Custom(
                    semicolon,
                    mutableListOf(charset to utf8, name to value),
                )
            }
            is ParamSource.Custom -> {
                p.params.add(name to value)
            }
            is ParamSource.None -> {
                if (semicolon + 2 == name.first &&
                    nameEqStr(CHARSET, s.substring(name.first, name.second))
                ) {
                    if (nameEqStr(UTF_8, s.substring(value.first, value.second))) {
                        params = ParamSource.Utf8(semicolon)
                        continue@paramsLoop
                    }
                }
                params = ParamSource.Custom(
                    semicolon,
                    mutableListOf(name to value),
                )
            }
        }
    }
    return params
}

private fun lowerAsciiWithParams(
    s: String,
    semi: Int,
    params: List<Pair<Indexed, Indexed>>,
): String {
    val owned = StringBuilder(s)
    for (k in 0 until semi) {
        owned[k] = owned[k].lowercaseChar()
    }

    for ((nameIdx, valueIdx) in params) {
        for (k in nameIdx.first until nameIdx.second) {
            owned[k] = owned[k].lowercaseChar()
        }
        // Since we just converted this part of the string to lowercase,
        // we can skip the Name == String unicase check and do a faster
        // memcmp instead.
        if (owned.substring(nameIdx.first, nameIdx.second) == CHARSET.source) {
            for (k in valueIdx.first until valueIdx.second) {
                owned[k] = owned[k].lowercaseChar()
            }
        }
    }

    return owned.toString()
}

// From [RFC6838](http://tools.ietf.org/html/rfc6838#section-4.2):
//
// > All registered media types MUST be assigned top-level type and
// > subtype names.  The combination of these names serves to uniquely
// > identify the media type, and the subtype name facet (or the absence
// > of one) identifies the registration tree.  Both top-level type and
// > subtype names are case-insensitive.
// >
// > Type and subtype names MUST conform to the following ABNF:
// >
// >     type-name = restricted-name
// >     subtype-name = restricted-name
// >
// >     restricted-name = restricted-name-first *126restricted-name-chars
// >     restricted-name-first  = ALPHA / DIGIT
// >     restricted-name-chars  = ALPHA / DIGIT / "!" / "#" /
// >                              "$" / "&" / "-" / "^" / "_"
// >     restricted-name-chars =/ "." ; Characters before first dot always
// >                                  ; specify a facet name
// >     restricted-name-chars =/ "+" ; Characters after last plus always
// >                                  ; specify a structured syntax suffix

// However, [HTTP](https://tools.ietf.org/html/rfc7231#section-3.1.1.1):
//
// >     media-type = type "/" subtype *( OWS ";" OWS parameter )
// >     type       = token
// >     subtype    = token
// >     parameter  = token "=" ( token / quoted-string )
//
// Where token is defined as:
//
// >     token = 1*tchar
// >     tchar = "!" / "#" / "$" / "%" / "&" / "'" / "*" / "+" / "-" / "." /
// >        "^" / "_" / "`" / "|" / "~" / DIGIT / ALPHA
//
// So, clearly, the parser accepts the HTTP token set.

internal val TOKEN_MAP: BooleanArray = booleanArrayOf(
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, true,  false, true,  true,  true,  true,  true,  false, false, true,  true,  false, true,  true,  false,
    true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false, false, false, false, false, false,
    false, true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false, false, false, true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,
    true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  false, true,  false, true,  false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
    false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
)

internal fun isToken(c: Int): Boolean = TOKEN_MAP[c]

internal fun isRestrictedQuotedChar(c: Int): Boolean = c > 31 && c != 127
