// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

internal sealed class ParamsInner {
    object Utf8 : ParamsInner()
    class Custom(val source: Source, val params: Iterator<Pair<Indexed, Indexed>>) : ParamsInner()
    object None : ParamsInner()
}

/**
 * An iterator over the parameters of a MIME.
 */
class Params internal constructor(private var inner: ParamsInner) : Iterator<Pair<Name, Name>> {

    private var cached: Pair<Name, Name>? = null
    private var done: Boolean = false

    private fun advance(): Pair<Name, Name>? {
        return when (val current = inner) {
            is ParamsInner.Utf8 -> {
                val value = CHARSET to UTF_8
                inner = ParamsInner.None
                value
            }
            is ParamsInner.Custom -> {
                if (!current.params.hasNext()) return null
                val (nameIdx, valueIdx) = current.params.next()
                val name = Name(
                    source = current.source.asRef().substring(nameIdx.first, nameIdx.second),
                    insensitive = true,
                )
                val value = Name(
                    source = current.source.asRef().substring(valueIdx.first, valueIdx.second),
                    insensitive = name == CHARSET,
                )
                name to value
            }
            is ParamsInner.None -> null
        }
    }

    private fun ensureCached() {
        if (done) return
        if (cached != null) return
        val nxt = advance()
        if (nxt == null) {
            done = true
        } else {
            cached = nxt
        }
    }

    override fun hasNext(): Boolean {
        ensureCached()
        return !done
    }

    override fun next(): Pair<Name, Name> {
        ensureCached()
        val c = cached ?: throw NoSuchElementException()
        cached = null
        return c
    }

    override fun toString(): String = "Params"
}
