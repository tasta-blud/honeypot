package honeypot.api

import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget


fun <T : Element> T.text(text: String): T = createText(text)
fun <T : EventTarget> T.onClick(callback: T.(Event) -> Unit) =
    addListener("click", callback)

fun <T : EventTarget> T.onChange(callback: T.(Event) -> Unit) =
    addListener("change", callback)

fun <T : EventTarget> T.onFocus(callback: T.(Event) -> Unit) =
    addListener("focus", callback)

fun <T : EventTarget> T.onBlur(callback: T.(Event) -> Unit) =
    addListener("blur", callback)

fun <T : EventTarget> T.onKeyUp(callback: T.(Event) -> Unit) =
    addListener("keyup", callback)

fun <T : EventTarget> T.onKeyDown(callback: T.(Event) -> Unit) =
    addListener("keydown", callback)

fun Element.div(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLDivElement.() -> Unit = {}
): HTMLDivElement =
    createElement("div", classes, attrs, init)

fun Element.span(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLSpanElement.() -> Unit = {}
): HTMLSpanElement =
    createElement("span", classes, attrs, init)

fun Element.h1(
    text: String = "",
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLHeadingElement.() -> Unit = {}
): HTMLHeadingElement =
    createElement("h1", classes, attrs, init).createText(text)

fun Element.h2(
    text: String = "",
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLHeadingElement.() -> Unit = {}
): HTMLHeadingElement =
    createElement("h2", classes, attrs, init).createText(text)

fun Element.h3(
    text: String = "",
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLHeadingElement.() -> Unit = {}
): HTMLHeadingElement =
    createElement("h3", classes, attrs, init).createText(text)

fun Element.hr(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLHRElement.() -> Unit = {}
): HTMLHRElement =
    createElement("hr", classes, attrs, init)

fun Element.input(
    type: String,
    value: String = "",
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLInputElement.() -> Unit = {}
): HTMLInputElement =
    createElement("input", classes, attrs) { this.type = type; this.value = value; init() }

fun Element.label(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLLabelElement.() -> Unit = {}
): HTMLLabelElement =
    createElement("label", classes, attrs, init)

fun Element.table(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLTableElement.() -> Unit = {}
): HTMLTableElement =
    createElement("table", classes, attrs, init)

fun HTMLTableElement.caption(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLTableCaptionElement.() -> Unit = {}
): HTMLTableCaptionElement =
    createElement("caption", classes, attrs, init)

fun Element.tr(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLTableRowElement.() -> Unit = {}
): HTMLTableRowElement =
    createElement("tr", classes, attrs, init)

fun Element.td(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLTableCellElement.() -> Unit = {}
): HTMLTableCellElement =
    createElement("td", classes, attrs, init)

fun Element.th(
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLTableCellElement.() -> Unit = {}
): HTMLTableCellElement =
    createElement("th", classes, attrs, init)

fun Element.a(
    url: String = "#",
    classes: String? = null,
    attrs: Map<String, String>? = null,
    init: HTMLAnchorElement.() -> Unit = {}
): HTMLAnchorElement =
    createElement("a", classes, attrs) { href = url; init() }
