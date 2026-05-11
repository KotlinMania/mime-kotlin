// port-lint: source lib.rs
package io.github.kotlinmania.mime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeTest {

    @Test
    fun testNamesMacroConsts() {
        val all = listOf(
            STAR,
            TEXT, IMAGE, AUDIO, VIDEO, APPLICATION, MULTIPART, MESSAGE, MODEL, FONT,
            PLAIN, HTML, XML, JAVASCRIPT, CSS, CSV, EVENT_STREAM, VCARD,
            JSON, WWW_FORM_URLENCODED, MSGPACK, OCTET_STREAM, PDF,
            WOFF, WOFF2,
            FORM_DATA,
            BMP, GIF, JPEG, PNG, SVG,
            BASIC, MPEG, MP4, OGG,
            CHARSET, BOUNDARY, UTF_8,
        )
        for (name in all) {
            assertEquals(name.source.lowercase(), name.source)
        }
    }

    @Test
    fun testMimesMacroConsts() {
        // For every Mime constant, verify:
        //  - the byte at `slash` is '/'
        //  - if `plus` is set, the byte at `plus` is '+'; otherwise no '+' present
        //  - if params is Utf8, the byte at `semicolon` is ';' and the suffix is "; charset=utf-8";
        //    if params is None, no ';' present
        //  - the atom() value is the position+1 in the declaration order
        data class Case(val mime: Mime, val expectedAtom: Int)
        val cases = listOf(
            Case(STAR_STAR, 1),
            Case(TEXT_STAR, 2),
            Case(TEXT_PLAIN, 3),
            Case(TEXT_PLAIN_UTF_8, 4),
            Case(TEXT_HTML, 5),
            Case(TEXT_HTML_UTF_8, 6),
            Case(TEXT_CSS, 7),
            Case(TEXT_CSS_UTF_8, 8),
            Case(TEXT_JAVASCRIPT, 9),
            Case(TEXT_XML, 10),
            Case(TEXT_EVENT_STREAM, 11),
            Case(TEXT_CSV, 12),
            Case(TEXT_CSV_UTF_8, 13),
            Case(TEXT_TAB_SEPARATED_VALUES, 14),
            Case(TEXT_TAB_SEPARATED_VALUES_UTF_8, 15),
            Case(TEXT_VCARD, 16),
            Case(IMAGE_STAR, 17),
            Case(IMAGE_JPEG, 18),
            Case(IMAGE_GIF, 19),
            Case(IMAGE_PNG, 20),
            Case(IMAGE_BMP, 21),
            Case(IMAGE_SVG, 22),
            Case(FONT_WOFF, 23),
            Case(FONT_WOFF2, 24),
            Case(APPLICATION_JSON, 25),
            Case(APPLICATION_JAVASCRIPT, 26),
            Case(APPLICATION_JAVASCRIPT_UTF_8, 27),
            Case(APPLICATION_WWW_FORM_URLENCODED, 28),
            Case(APPLICATION_OCTET_STREAM, 29),
            Case(APPLICATION_MSGPACK, 30),
            Case(APPLICATION_PDF, 31),
            Case(MULTIPART_FORM_DATA, 32),
        )
        for ((pos, case) in cases.withIndex()) {
            val mime = case.mime
            val asStr = mime.asStr()
            assertEquals('/', asStr[mime.slash], "$mime has ${asStr[mime.slash]} at slash position ${mime.slash}")
            val plus = mime.plus
            if (plus != null) {
                assertEquals('+', asStr[plus], "$mime has ${asStr[plus]} at plus position $plus")
            } else {
                assertFalse(asStr.contains('+'), "$mime forgot plus")
            }
            when (val p = mime.params) {
                is ParamSource.Utf8 -> {
                    assertEquals(';', asStr[p.semicolon])
                    assertEquals("; charset=utf-8", asStr.substring(p.semicolon))
                }
                is ParamSource.None -> {
                    assertFalse(asStr.contains(';'))
                }
                else -> error("unreachable")
            }
            assertEquals(pos + 1, mime.atom(), "atom ${mime.atom()} in position ${pos + 1}")
            assertEquals(case.expectedAtom, mime.atom())
        }
    }

    @Test
    fun testType() {
        assertEquals(TEXT, TEXT_PLAIN.type())
    }

    @Test
    fun testSubtype() {
        assertEquals(PLAIN, TEXT_PLAIN.subtype())
        assertEquals(PLAIN, TEXT_PLAIN_UTF_8.subtype())
        val mime = fromStr("text/html+xml")
        assertEquals(HTML, mime.subtype())
    }

    @Test
    fun testMatching() {
        val t = TEXT_PLAIN.type()
        val s = TEXT_PLAIN.subtype()
        assertTrue(t == TEXT && s == PLAIN)
    }

    @Test
    fun testSuffix() {
        assertNull(TEXT_PLAIN.suffix())
        val mime = fromStr("text/html+xml")
        assertEquals(XML, mime.suffix())
    }

    @Test
    fun testMimeFmt() {
        val mime = TEXT_PLAIN
        assertEquals("text/plain", mime.toString())
        val mime2 = TEXT_PLAIN_UTF_8
        assertEquals("text/plain; charset=utf-8", mime2.toString())
    }

    @Test
    fun testMimeFromStr() {
        assertEquals(TEXT_PLAIN, fromStr("text/plain"))
        assertEquals(TEXT_PLAIN, fromStr("TEXT/PLAIN"))
        assertEquals(TEXT_PLAIN_UTF_8, fromStr("text/plain;charset=utf-8"))
        assertEquals(TEXT_PLAIN_UTF_8, fromStr("text/plain;charset=\"utf-8\""))

        // spaces
        assertEquals(TEXT_PLAIN_UTF_8, fromStr("text/plain; charset=utf-8"))

        // quotes + semi colon
        fromStr("text/plain;charset=\"utf-8\"; foo=bar")
        fromStr("text/plain;charset=\"utf-8\" ; foo=bar")

        val upper = fromStr("TEXT/PLAIN")
        assertEquals(TEXT_PLAIN, upper)
        assertEquals(TEXT, upper.type())
        assertEquals(PLAIN, upper.subtype())

        val extended = fromStr("TEXT/PLAIN; CHARSET=UTF-8; FOO=BAR")
        assertTrue(extended.equals("text/plain; charset=utf-8; foo=BAR"))
        assertEquals("utf-8", extended.getParam("charset").toString())
        assertEquals("BAR", extended.getParam("foo").toString())

        fromStr("multipart/form-data; boundary=--------foobar")

        // stars
        assertEquals(STAR_STAR, fromStr("*/*"))
        assertTrue(fromStr("image/*").equals("image/*"))
        assertTrue(fromStr("text/*; charset=utf-8").equals("text/*; charset=utf-8"))

        // parse errors
        assertFailsWith<FromStrError> { fromStr("f o o / bar") }
        assertFailsWith<FromStrError> { fromStr("text\n/plain") }
        assertFailsWith<FromStrError> { fromStr("text\r/plain") }
        assertFailsWith<FromStrError> { fromStr("text/\r\nplain") }
        assertFailsWith<FromStrError> { fromStr("text/plain;\r\ncharset=utf-8") }
        assertFailsWith<FromStrError> { fromStr("text/plain; charset=\r\nutf-8") }
        assertFailsWith<FromStrError> { fromStr("text/plain; charset=\"\r\nutf-8\"") }
    }

    @Test
    fun testMimeFromStrEmptyParameterList() {
        val cases = listOf(
            "text/event-stream;",
            "text/event-stream; ",
            "text/event-stream;       ",
        )

        for (case in cases) {
            val mime = fromStr(case)
            assertEquals(TEXT_EVENT_STREAM, mime, "case = \"$case\"")
            assertEquals(TEXT, mime.type(), "case = \"$case\"")
            assertEquals(EVENT_STREAM, mime.subtype(), "case = \"$case\"")
            assertFalse(mime.hasParams(), "case = \"$case\"")
        }
    }

    @Test
    fun testCaseSensitiveValues() {
        val mime = fromStr("multipart/form-data; charset=BASE64; boundary=ABCDEFG")
        assertTrue(mime.getParam(CHARSET)!!.equals("bAsE64"))
        assertTrue(mime.getParam(BOUNDARY)!!.equals("ABCDEFG"))
        assertFalse(mime.getParam(BOUNDARY)!!.equals("abcdefg"))
    }

    @Test
    fun testGetParam() {
        assertNull(TEXT_PLAIN.getParam("charset"))
        assertNull(TEXT_PLAIN.getParam("baz"))

        assertEquals(UTF_8, TEXT_PLAIN_UTF_8.getParam("charset"))
        assertNull(TEXT_PLAIN_UTF_8.getParam("baz"))

        val mime = fromStr("text/plain; charset=utf-8; foo=bar")
        assertEquals("utf-8", mime.getParam(CHARSET).toString())
        assertEquals("bar", mime.getParam("foo").toString())
        assertNull(mime.getParam("baz"))

        val mime2 = fromStr("text/plain;charset=\"utf-8\"")
        assertEquals(UTF_8, mime2.getParam(CHARSET))
    }

    @Test
    fun testNameEq() {
        assertEquals(TEXT, TEXT)
        assertTrue(TEXT.equals("text"))
        assertTrue(TEXT.equals("TEXT"))

        val param = Name(source = "ABC", insensitive = false)

        assertEquals(param, param)
        assertTrue(param.equals("ABC"))
        assertFalse(param.equals("abc"))
    }

    @Test
    fun testEssenceStr() {
        assertEquals("text/plain", TEXT_PLAIN.essenceStr())
        assertEquals("text/plain", TEXT_PLAIN_UTF_8.essenceStr())
        assertEquals("image/svg+xml", IMAGE_SVG.essenceStr())
    }
}
