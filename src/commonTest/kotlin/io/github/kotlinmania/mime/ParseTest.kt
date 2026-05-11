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

    @Test
    fun testParseIteratorAllInvalid() {
        val iter = MimeIter("application/json, text/html")
        assertEquals("application/json", (iter.next() as MimeIter.Item.Err).slice)
        assertFalse(iter.hasNext())
    }
}
