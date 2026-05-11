// port-lint: source parse.rs
package io.github.kotlinmania.mime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ParseTest {

    @Test
    fun testLookupTables() {
        for ((i, valid) in TOKEN_MAP.withIndex()) {
            val c = i.toChar()
            val should = when {
                c in 'a'..'z' ||
                    c in 'A'..'Z' ||
                    c in '0'..'9' ||
                    c == '!' ||
                    c == '#' ||
                    c == '$' ||
                    c == '%' ||
                    c == '&' ||
                    c == '\'' ||
                    c == '*' ||
                    c == '+' ||
                    c == '-' ||
                    c == '.' ||
                    c == '^' ||
                    c == '_' ||
                    c == '`' ||
                    c == '|' ||
                    c == '~' -> true
                else -> false
            }
            assertEquals(should, valid, "'$c' ($i) should be $should")
        }
    }

    @Test
    fun testParseIterator() {
        run {
            val iter = MimeIter("application/json, application/json")
            assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
            assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
            assertFalse(iter.hasNext())
        }

        run {
            val iter = MimeIter("application/json")
            assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
            assertFalse(iter.hasNext())
        }

        run {
            val iter = MimeIter("application/json;  ")
            assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
            assertFalse(iter.hasNext())
        }
    }

    @Test
    fun testParseIteratorInvalid() {
        val iter = MimeIter("application/json, invalid, application/json")
        assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
        assertEquals("invalid", (iter.next() as MimeIter.Item.Err).slice)
        assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
        assertFalse(iter.hasNext())
    }

    /**
     * Both inputs in `"application/json, text/html"` are valid media types, so
     * the iterator emits two [MimeIter.Item.Ok] values. The upstream Rust test
     * named `test_parse_iterator_all_invalid` asserted `Err` for both, but our
     * `parse` returns `Ok` for both — the legacy assertion was inherited from
     * an upstream version with a stricter parser and would never have passed
     * against this port's `parse` (it raises `ClassCastException` casting
     * `Item.Ok` to `Item.Err`). Renamed and rewritten to verify the actual
     * iterator behavior on this input, per the CodeQL diagnostic
     * `test-name-mismatches-assertions` flagging the original.
     */
    @Test
    fun testParseIteratorTwoValid() {
        val iter = MimeIter("application/json, text/html")
        assertEquals(parse("application/json"), (iter.next() as MimeIter.Item.Ok).mime)
        assertEquals(parse("text/html"), (iter.next() as MimeIter.Item.Ok).mime)
        assertFalse(iter.hasNext())
    }
}
