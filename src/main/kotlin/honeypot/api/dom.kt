package honeypot.api

import kotlinx.dom.createElement
import org.w3c.dom.Element
import org.w3c.dom.HTMLCollection
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

fun <T : Element> Element.createElement(
    tag: String,
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: T.() -> Unit = {}
): T =
    ownerDocument!!.createElement(tag) {
        classes?.also { setAttribute("class", it) }
        attrs?.forEach { (key: String, value: String) -> setAttribute(key, value) }
    }.unsafeCast<T>().apply(init).also(::appendChild)

fun <T : Element> T.createText(text: String): T =
    ownerDocument!!.createTextNode(text).also(::appendChild).let { this }

fun <T : Element> Element.findElement(id: String): T? =
    ownerDocument!!.getElementById(id).unsafeCast<T?>()

fun <T : Element> Element.getElement(id: String): T =
    findElement(id) ?: error("element '$id' not found")

fun <T : EventTarget> T.addListener(type: String, callback: T.(Event) -> Unit): T =
    unsafeCast<T>().apply { addEventListener(type, { this.callback(it)}) }

operator fun HTMLCollection.iterator(): Iterator<Element> =
    object : Iterator<Element> {
        private var i = 0
        override fun hasNext(): Boolean = i < length
        override fun next(): Element = item(i).also { i++ } ?: error("No such Element")
    }

fun HTMLCollection.toList(): List<Element> =
    List(length) { item(it) ?: error("No such Element") }
