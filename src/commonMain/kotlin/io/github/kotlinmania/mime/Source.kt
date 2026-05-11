// port-lint: source lib.rs
package io.github.kotlinmania.mime

internal sealed class Source {
    class Atom(val tag: Int, val text: String) : Source()
    class Dynamic(val text: String) : Source()

    fun asRef(): String = when (this) {
        is Atom -> text
        is Dynamic -> text
    }
}
