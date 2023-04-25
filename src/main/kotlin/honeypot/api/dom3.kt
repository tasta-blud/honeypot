package honeypot.api

import org.w3c.dom.Element
import org.w3c.dom.HTMLStyleElement

fun Element.addStyle(vararg styles: String) {
    ownerDocument!!.head!!.createElement<HTMLStyleElement>("style").textContent = styles.joinToString("\n")
}
