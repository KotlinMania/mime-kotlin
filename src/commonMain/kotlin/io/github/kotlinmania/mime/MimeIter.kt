// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

/**
 * An iterator of parsed mime.
 */
class MimeIter(private val sourceText: String) : Iterator<MimeIter.Item> {
    private var pos: Int = 0
    private var cached: Item? = null
    private var done: Boolean = false

    /** Result variant returned by the iterator: either a parsed [Mime] or the offending substring. */
    sealed class Item {
        data class Ok(val mime: Mime) : Item()
        data class Err(val slice: String) : Item()
    }

    private fun advance(): Item? {
        val start = pos
        val len = sourceText.length

        if (start >= len) {
            return null
        }

        // Try parsing the whole remaining slice, until the end.
        try {
            val value = parse(sourceText.substring(start, len))
            pos = len
            return Item.Ok(value)
        } catch (e: ParseError.InvalidToken) {
            // The first token is immediately found to be wrong by `parse`. Skip it.
            if (e.pos == 0) {
                pos += 1
                return advance()
            }
            val slice = sourceText.substring(start, start + e.pos)
            // Try parsing the longest slice (until the first invalid token)
            return try {
                val mime = parse(slice)
                pos = start + e.pos + 1
                Item.Ok(mime)
            } catch (_: ParseError) {
                if (start + e.pos < len) {
                    // Skip this invalid slice,
                    // try parsing the remaining slice in the next iteration
                    pos = start + e.pos
                    Item.Err(slice)
                } else {
                    null
                }
            }
        } catch (_: ParseError) {
            // Do not process any other error condition: the slice is malformed and
            // no character is found to be invalid: a character is missing
            return null
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

    override fun next(): Item {
        ensureCached()
        val c = cached ?: throw NoSuchElementException()
        cached = null
        return c
    }
}
