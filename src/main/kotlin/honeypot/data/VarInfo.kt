package honeypot.data

import honeypot.util.LateVal
import org.w3c.dom.HTMLInputElement

data class VarInfo(val path: String, val type: String, var value: Any? = null) {
    var locked: Boolean = false
    var lockedValue: Any? = null
    var editor: HTMLInputElement by LateVal()
}
