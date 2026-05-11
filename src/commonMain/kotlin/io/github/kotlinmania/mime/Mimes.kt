// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

internal object Atoms {
    const val DYNAMIC = 0
    const val STAR_STAR = 1
    const val TEXT_STAR = 2
    const val TEXT_PLAIN = 3
    const val TEXT_PLAIN_UTF_8 = 4
    const val TEXT_HTML = 5
    const val TEXT_HTML_UTF_8 = 6
    const val TEXT_CSS = 7
    const val TEXT_CSS_UTF_8 = 8
    const val TEXT_JAVASCRIPT = 9
    const val TEXT_XML = 10
    const val TEXT_EVENT_STREAM = 11
    const val TEXT_CSV = 12
    const val TEXT_CSV_UTF_8 = 13
    const val TEXT_TAB_SEPARATED_VALUES = 14
    const val TEXT_TAB_SEPARATED_VALUES_UTF_8 = 15
    const val TEXT_VCARD = 16
    const val IMAGE_STAR = 17
    const val IMAGE_JPEG = 18
    const val IMAGE_GIF = 19
    const val IMAGE_PNG = 20
    const val IMAGE_BMP = 21
    const val IMAGE_SVG = 22
    const val FONT_WOFF = 23
    const val FONT_WOFF2 = 24
    const val APPLICATION_JSON = 25
    const val APPLICATION_JAVASCRIPT = 26
    const val APPLICATION_JAVASCRIPT_UTF_8 = 27
    const val APPLICATION_WWW_FORM_URLENCODED = 28
    const val APPLICATION_OCTET_STREAM = 29
    const val APPLICATION_MSGPACK = 30
    const val APPLICATION_PDF = 31
    const val MULTIPART_FORM_DATA = 32
}

private fun mimeConst(tag: Int, src: String, slash: Int, plus: Int? = null, semicolon: Int? = null): Mime =
    Mime(
        source = Source.Atom(tag, src),
        slash = slash,
        plus = plus,
        params = if (semicolon != null) ParamSource.Utf8(semicolon) else ParamSource.None,
    )

// `*/*`
val STAR_STAR: Mime = mimeConst(Atoms.STAR_STAR, "*/*", 1)

// `text/*`
val TEXT_STAR: Mime = mimeConst(Atoms.TEXT_STAR, "text/*", 4)
// `text/plain`
val TEXT_PLAIN: Mime = mimeConst(Atoms.TEXT_PLAIN, "text/plain", 4)
// `text/plain; charset=utf-8`
val TEXT_PLAIN_UTF_8: Mime = mimeConst(Atoms.TEXT_PLAIN_UTF_8, "text/plain; charset=utf-8", 4, null, 10)
// `text/html`
val TEXT_HTML: Mime = mimeConst(Atoms.TEXT_HTML, "text/html", 4)
// `text/html; charset=utf-8`
val TEXT_HTML_UTF_8: Mime = mimeConst(Atoms.TEXT_HTML_UTF_8, "text/html; charset=utf-8", 4, null, 9)
// `text/css`
val TEXT_CSS: Mime = mimeConst(Atoms.TEXT_CSS, "text/css", 4)
// `text/css; charset=utf-8`
val TEXT_CSS_UTF_8: Mime = mimeConst(Atoms.TEXT_CSS_UTF_8, "text/css; charset=utf-8", 4, null, 8)
// `text/javascript`
val TEXT_JAVASCRIPT: Mime = mimeConst(Atoms.TEXT_JAVASCRIPT, "text/javascript", 4)
// `text/xml`
val TEXT_XML: Mime = mimeConst(Atoms.TEXT_XML, "text/xml", 4)
// `text/event-stream`
val TEXT_EVENT_STREAM: Mime = mimeConst(Atoms.TEXT_EVENT_STREAM, "text/event-stream", 4)
// `text/csv`
val TEXT_CSV: Mime = mimeConst(Atoms.TEXT_CSV, "text/csv", 4)
// `text/csv; charset=utf-8`
val TEXT_CSV_UTF_8: Mime = mimeConst(Atoms.TEXT_CSV_UTF_8, "text/csv; charset=utf-8", 4, null, 8)
// `text/tab-separated-values`
val TEXT_TAB_SEPARATED_VALUES: Mime = mimeConst(Atoms.TEXT_TAB_SEPARATED_VALUES, "text/tab-separated-values", 4)
// `text/tab-separated-values; charset=utf-8`
val TEXT_TAB_SEPARATED_VALUES_UTF_8: Mime = mimeConst(
    Atoms.TEXT_TAB_SEPARATED_VALUES_UTF_8, "text/tab-separated-values; charset=utf-8", 4, null, 25,
)
// `text/vcard`
val TEXT_VCARD: Mime = mimeConst(Atoms.TEXT_VCARD, "text/vcard", 4)

// `image/*`
val IMAGE_STAR: Mime = mimeConst(Atoms.IMAGE_STAR, "image/*", 5)
// `image/jpeg`
val IMAGE_JPEG: Mime = mimeConst(Atoms.IMAGE_JPEG, "image/jpeg", 5)
// `image/gif`
val IMAGE_GIF: Mime = mimeConst(Atoms.IMAGE_GIF, "image/gif", 5)
// `image/png`
val IMAGE_PNG: Mime = mimeConst(Atoms.IMAGE_PNG, "image/png", 5)
// `image/bmp`
val IMAGE_BMP: Mime = mimeConst(Atoms.IMAGE_BMP, "image/bmp", 5)
// `image/svg+xml`
val IMAGE_SVG: Mime = mimeConst(Atoms.IMAGE_SVG, "image/svg+xml", 5, 9)

// `font/woff`
val FONT_WOFF: Mime = mimeConst(Atoms.FONT_WOFF, "font/woff", 4)
// `font/woff2`
val FONT_WOFF2: Mime = mimeConst(Atoms.FONT_WOFF2, "font/woff2", 4)

// `application/json`
val APPLICATION_JSON: Mime = mimeConst(Atoms.APPLICATION_JSON, "application/json", 11)
// `application/javascript`
val APPLICATION_JAVASCRIPT: Mime = mimeConst(Atoms.APPLICATION_JAVASCRIPT, "application/javascript", 11)
// `application/javascript; charset=utf-8`
val APPLICATION_JAVASCRIPT_UTF_8: Mime = mimeConst(
    Atoms.APPLICATION_JAVASCRIPT_UTF_8, "application/javascript; charset=utf-8", 11, null, 22,
)
// `application/x-www-form-urlencoded`
val APPLICATION_WWW_FORM_URLENCODED: Mime = mimeConst(
    Atoms.APPLICATION_WWW_FORM_URLENCODED, "application/x-www-form-urlencoded", 11,
)
// `application/octet-stream`
val APPLICATION_OCTET_STREAM: Mime = mimeConst(Atoms.APPLICATION_OCTET_STREAM, "application/octet-stream", 11)
// `application/msgpack`
val APPLICATION_MSGPACK: Mime = mimeConst(Atoms.APPLICATION_MSGPACK, "application/msgpack", 11)
// `application/pdf`
val APPLICATION_PDF: Mime = mimeConst(Atoms.APPLICATION_PDF, "application/pdf", 11)

// `multipart/form-data`
val MULTIPART_FORM_DATA: Mime = mimeConst(Atoms.MULTIPART_FORM_DATA, "multipart/form-data", 9)

@Deprecated("please use TEXT_JAVASCRIPT instead", ReplaceWith("TEXT_JAVASCRIPT"))
val TEXT_JAVSCRIPT: Mime = TEXT_JAVASCRIPT
