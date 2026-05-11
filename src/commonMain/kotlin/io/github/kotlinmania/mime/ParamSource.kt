// port-lint: source src/lib.rs
package io.github.kotlinmania.mime

internal sealed class ParamSource {
    class Utf8(val semicolon: Int) : ParamSource()
    class Custom(val semicolon: Int, val params: MutableList<Pair<Indexed, Indexed>>) : ParamSource()
    object None : ParamSource()
}
