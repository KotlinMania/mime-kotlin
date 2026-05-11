// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

/** `*` */
val STAR: Name = Name(source = "*", insensitive = true)

/** `text` */
val TEXT: Name = Name(source = "text", insensitive = true)
/** `image` */
val IMAGE: Name = Name(source = "image", insensitive = true)
/** `audio` */
val AUDIO: Name = Name(source = "audio", insensitive = true)
/** `video` */
val VIDEO: Name = Name(source = "video", insensitive = true)
/** `application` */
val APPLICATION: Name = Name(source = "application", insensitive = true)
/** `multipart` */
val MULTIPART: Name = Name(source = "multipart", insensitive = true)
/** `message` */
val MESSAGE: Name = Name(source = "message", insensitive = true)
/** `model` */
val MODEL: Name = Name(source = "model", insensitive = true)
/** `font` */
val FONT: Name = Name(source = "font", insensitive = true)

// common text/ *
/** `plain` */
val PLAIN: Name = Name(source = "plain", insensitive = true)
/** `html` */
val HTML: Name = Name(source = "html", insensitive = true)
/** `xml` */
val XML: Name = Name(source = "xml", insensitive = true)
/** `javascript` */
val JAVASCRIPT: Name = Name(source = "javascript", insensitive = true)
/** `css` */
val CSS: Name = Name(source = "css", insensitive = true)
/** `csv` */
val CSV: Name = Name(source = "csv", insensitive = true)
/** `event-stream` */
val EVENT_STREAM: Name = Name(source = "event-stream", insensitive = true)
/** `vcard` */
val VCARD: Name = Name(source = "vcard", insensitive = true)

// common application/*
/** `json` */
val JSON: Name = Name(source = "json", insensitive = true)
/** `x-www-form-urlencoded` */
val WWW_FORM_URLENCODED: Name = Name(source = "x-www-form-urlencoded", insensitive = true)
/** `msgpack` */
val MSGPACK: Name = Name(source = "msgpack", insensitive = true)
/** `octet-stream` */
val OCTET_STREAM: Name = Name(source = "octet-stream", insensitive = true)
/** `pdf` */
val PDF: Name = Name(source = "pdf", insensitive = true)

// common font/*
/** `woff` */
val WOFF: Name = Name(source = "woff", insensitive = true)
/** `woff2` */
val WOFF2: Name = Name(source = "woff2", insensitive = true)

// multipart/*
/** `form-data` */
val FORM_DATA: Name = Name(source = "form-data", insensitive = true)

// common image/*
/** `bmp` */
val BMP: Name = Name(source = "bmp", insensitive = true)
/** `gif` */
val GIF: Name = Name(source = "gif", insensitive = true)
/** `jpeg` */
val JPEG: Name = Name(source = "jpeg", insensitive = true)
/** `png` */
val PNG: Name = Name(source = "png", insensitive = true)
/** `svg` */
val SVG: Name = Name(source = "svg", insensitive = true)

// audio/*
/** `basic` */
val BASIC: Name = Name(source = "basic", insensitive = true)
/** `mpeg` */
val MPEG: Name = Name(source = "mpeg", insensitive = true)
/** `mp4` */
val MP4: Name = Name(source = "mp4", insensitive = true)
/** `ogg` */
val OGG: Name = Name(source = "ogg", insensitive = true)

// parameters
/** `charset` */
val CHARSET: Name = Name(source = "charset", insensitive = true)
/** `boundary` */
val BOUNDARY: Name = Name(source = "boundary", insensitive = true)
/** `utf-8` */
val UTF_8: Name = Name(source = "utf-8", insensitive = true)
