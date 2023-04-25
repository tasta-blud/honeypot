package honeypot

import honeypot.api.*
import honeypot.data.VarInfo
import org.w3c.dom.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.js.Json

@OptIn(ExperimentalJsExport::class)
@JsExport
class HoneyPot private constructor(private val root: Element) {
    companion object {
        private var honeyPot: HoneyPot? = null

        @Suppress("unused")
        fun construct() =
            getManifest().apply {
                createPanel(
                    Messages.panel_title(),
                    getJson("icons")?.getString("16") ?: error("no icon"),
                    "panel.html",
                    { honeyPot = HoneyPot(it.document.getElementById("root") ?: error("no root")) },
                    { honeyPot?.destroy();honeyPot = null })
            }
    }

    @Suppress("SpellCheckingInspection")
    private val engines = mapOf(
        "SugarCube1" to "SugarCube.state.active.variables",
        "SugarCube2" to "SugarCube.State.active.variables",
        "wetgame" to "wetgame.state.story.variablesState._globalVariables"
    )

    private val options = HoneyPotOptions(root) { scheduleUpdate() }

    init {
        initTools()
        root.table()
        root.hr()
        detectEngines()
    }

    private fun detectEngines() {
        val content = root.div { id = "content" }
        val h1 = content.h1()
        val message = content.div("message")
        var tries = 0
        engines.forEach { (_: String, _: String) -> tries++ }
        evaluate<String>("window.document.title", { title ->
            h1.textContent = title
            engines.forEach { (key: String, value: String) ->
                evaluate<Json?>("try{${value}}catch(e){null}", { vars ->
                    if (vars != null && rootExpression.isBlank()) {
                        messageUi(Messages.engines_detected_success(key), message, "success")
                        inspected(value, vars, content.div())
                    }
                }, {
                    tries--
                    if (tries == 0)
                        messageUi(Messages.engines_detected_error(), message, "error")
                })
            }
        }, { alert(Messages.panel_error(it.message ?: ""), it) })
    }


    private var rootExpression: String = ""
    private val data: MutableMap<String, VarInfo> = mutableMapOf()


    fun destroy() {
        root.innerHTML = ""
        rootExpression = ""
        data.clear()
    }

    private fun inspected(expression: String, vars: Json, parent: Element) {
        rootExpression = expression
        data.clear()
        createData(vars, "", data)
        createUi(vars, "", parent)
        scheduleUpdate()
    }

    private fun getInPath(array: Any, path: List<String>): Any? {
        var cur: Any? = array
        for (pe in path) {
            cur = cur.asDynamic()[pe]
            if (cur == null) return null
        }
        return cur
    }

    private fun updateAllFields() {
        if (rootExpression.isNotBlank() && options.automatic) {
            evaluate<Any>(rootExpression, { vars ->
                data.forEach { (key: String, _: VarInfo) ->
                    updateFieldLock(key)
                    updateFieldValue(key, getInPath(vars, key.substring(1).split(".")), false)
                }
                if (options.automatic)
                    scheduleUpdate()
            }) { alert(Messages.panel_error_expression(rootExpression, it.message ?: ""), it) }
        }
    }

    private fun updateField(
        path: String,
        callback: (HTMLInputElement, Boolean, Boolean) -> Unit = { _, _, _ -> }
    ) {
        val expression = rootExpression + path.substring(1).split('.').joinToString("") { "['${it}']" }
        evaluate<Any>(expression, {
            updateFieldValue(path, it, true, callback)
        }
        ) { alert(Messages.panel_error_field(expression, it.message ?: ""), it) }
    }

    private fun updateFieldLock(path: String) {
        if (varInfo(path).locked)
            lockValue(path)
    }

    private fun updateFieldValue(
        path: String,
        newValue: Any?,
        inEditor: Boolean,
        callback: (HTMLInputElement, Boolean, Boolean) -> Unit = { _, _, _ -> }
    ) {
        val item: VarInfo = varInfo(path)
        val value: Any? = if (item.locked) item.lockedValue else newValue
        val changed = item.value != value
        if (changed) {
            val editorValue = toEditor(item.type, value)
            if (item.type == "boolean")
                item.editor.checked = editorValue.toBoolean()
            else
                item.editor.value = editorValue
            item.value = value
            updateFieldStyle(path, true)

        }
        callback(item.editor, changed, inEditor)
    }

    private fun scheduleUpdate() {
        root.ownerDocument?.defaultView?.setTimeout({ updateAllFields() }, options.interval)
    }

    private fun messageUi(message: String, parent: Element, type: String?) =
        parent.div("message ${if (type.isNullOrBlank()) "" else "message-${type}"}").text(message)

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")
    private fun createUi(obj: Any?, path: String, parent: Element): Element =
        when {
            obj == null -> parent.span("object-empty small").text("(null)")
            jsIsJson(obj) -> createUiJson(parent, path, obj as Json)
            jsIsArray(obj) -> createUiArray(parent, path, obj as Array<Any?>)
            jsTypeOf(obj) in listOf("bigint", "boolean", "number", "string") ->
                createUiPrimitive(parent, path, obj, jsTypeOf(obj))

            else -> parent.span("object-empty").text("(${jsTypeOf(obj)})")
        }

    private fun createUiJson(parent: Element, path: String, json: Json): HTMLTableElement {
        parent.classList.add("multiple")
        val keys = json.keys
        val table = parent.table("grid object ${if (keys.isEmpty()) "empty" else "collapsible"}") {
            id = "object-${getIdFor(path)}"
        }
        if (keys.isEmpty()) return table
        keys.sortedBy { it }.forEach { key ->
            val objectPath = "${path}.${key}"
            val tr = table.tr("row") {
                id = getIdForPath(objectPath)
            }
            tr.th("cell cell-label clickable") {
                onClick {
                    root.getElement<HTMLElement>("object-${getIdFor(objectPath)}").classList.toggle("collapsed")
                }
            }.label("label $key") {
                title = objectPath
            }.text(key.split("_").joinToString(" ") {
                it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ")
            })
            createUi(json[key], objectPath, tr.td("cell cell-data"))
        }
        return table
    }

    private fun createUiArray(parent: Element, path: String, array: Array<Any?>): HTMLTableElement {
        parent.classList.add("multiple")
        val table = parent.table("grid object ${if (array.isEmpty()) "empty" else "collapsible"}") {
            id = "object-${getIdFor(path)}"
        }
        if (array.isEmpty()) return table
        array.forEachIndexed { key, _ ->
            val objectPath = "${path}.${key}"
            val tr = table.tr("row") {
                id = getIdForPath(objectPath)
            }
            tr.th("cell cell-label clickable") {
                onClick {
                    root.getElement<HTMLElement>("object-${getIdFor(objectPath)}").classList.toggle("collapsed")
                }
            }.label("label $key") {
                title = objectPath
            }.text(key.toString())
            createUi(array[key], objectPath, tr.td("cell cell-data"))
        }
        return table
    }

    private fun createUiPrimitive(parent: Element, path: String, obj: Any, type: String): HTMLInputElement {
        parent.classList.add("single")
        val tooltip = path.substring(1).split('.').joinToString(": ") {
            it[0].uppercaseChar() + it.substring(1).split(Regex("(?=[A-Z])")).joinToString(" ")
                .split('_').joinToString(" ")
        }
        val editor =
            if (type == "boolean")
                createUiPrimitiveCheck(parent, path, obj, tooltip)
            else
                createUiPrimitiveInput(parent, path, obj, type, tooltip)
        varInfo(path).editor = editor
        val lockId = "lock_${getIdForPath(path)}"
        parent.let {
            it.input("checkbox", "locked", "editor-lock") {
                id = lockId
                title = Messages.panel_field_lock(tooltip)
                onClick { onLock(path, checked, editor.value) }
            }
            it.label {
                htmlFor = lockId
            }
        }
        varInfo(path).locked = false
        varInfo(path).lockedValue = varInfo(path).value
        return editor
    }

    private fun createUiPrimitiveInput(parent: Element, path: String, obj: Any, type: String, tooltip: String)
            : HTMLInputElement {
        val editor = parent.input(getInputType(type), toEditor(type, obj), "editor editor-$type") {
            this.title = tooltip
            this.onChange { onEdit(path, this.value) }
            this.onFocus {
                updateField(path) { input, _, _ ->
                    input.select()
                    updateFieldStyle(path, false)
                }
            }
            this.onBlur { onEdit(path, this.value) }
        }
        return editor
    }


    private fun createUiPrimitiveCheck(parent: Element, path: String, obj: Any, tooltip: String): HTMLInputElement {
        var editor: HTMLInputElement? = null
        parent.label("switch") {
            editor = input("checkbox", "true", "editor editor-boolean") {
                this.title = tooltip
                this.checked = toBoolean("boolean", obj)
                this.onChange { onEdit(path, this.checked) }
                this.onFocus {
                    updateField(path) { input, _, _ ->
                        input.select()
                        updateFieldStyle(path, false)
                    }
                }
                this.onClick { onEdit(path, this.checked) }
                this.onBlur { onEdit(path, this.checked) }
            }
            div("slider round")
        }
        return editor!!
    }

    private fun createData(obj: Any?, path: String, data: MutableMap<String, VarInfo>) {
        if (obj == null) return
        when {
            jsIsJson(obj) -> {
                @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
                (obj as Json).entries.forEach { (key: String, value: Any?) ->
                    createData(value, "${path}.${key}", data)
                }
            }

            jsIsArray(obj) -> {
                @Suppress("UNCHECKED_CAST")
                (obj as Array<Any?>).forEachIndexed { key: Int, value: Any? ->
                    createData(value, "${path}.${key}", data)
                }
            }

            jsTypeOf(obj) in listOf("bigint", "boolean", "number", "string") -> {
                data[path] = VarInfo(path, jsTypeOf(obj), obj)
            }
        }
    }

    private fun onEdit(path: String, value: Any) {
        val data: VarInfo = varInfo(path)
        if (data.locked)
            data.lockedValue = value
        if (data.value != fromEditor(data.type, value))
            setFieldValue(path, value)
    }

    private fun lockValue(path: String) {
        val data: VarInfo = varInfo(path)
        if (data.locked)
            setFieldValue(path, data.lockedValue)
    }

    private fun setFieldValue(path: String, value: Any?) {
        val data: VarInfo = varInfo(path)
        val fromEditorValue = fromEditor(data.type, value)
        updateFieldStyle(path, false)
        val expression = rootExpression + path.substring(1).split('.').joinToString("") { "['${it}']" } +
                "=${toExpression(data.type, value)};"
        evaluate<Any?>(expression, { data.value = fromEditorValue }) {
            alert("Cannot set value for $path as ${expression}: ${it.message}", it)
        }
    }

    private fun onLock(path: String, lock: Boolean, value: Any) {
        varInfo(path).locked = lock
        varInfo(path).lockedValue = value
        if (varInfo(path).value != value)
            setFieldValue(path, value)
    }

    private fun updateFieldStyle(path: String, changed: Boolean): Element =
        varInfo(path).editor.also {
            if (changed) it.classList.add("changed") else it.classList.remove("changed")
        }

    private fun clearAllFieldsStyle() {
        data.forEach { (key: String, _: VarInfo) -> updateFieldStyle(key, false) }
    }

    private fun getIdFor(path: String): String =
        path.replace('.', '-').lowercase()

    private fun getIdForPath(path: String): String =
        "path-${getIdFor(path)}"

    private fun filterSome(pattern: String) {
        data.forEach { (key: String, _: VarInfo) ->
            val id = getIdForPath(key)
            val element = root.getElement<HTMLElement>(id)
            if (pattern.isBlank() || pattern.lowercase() in id)
                element.classList.remove("hidden")
            else
                element.classList.add("hidden")
        }
        highlightSome(pattern)
    }

    private fun highlightSome(pattern: String) {
        root.getElementsByTagName("label").asList().forEach {
            if (pattern.isBlank() ||
                pattern.lowercase() !in it.classList.asList().joinToString(" ").lowercase()
            )
                it.classList.remove("highlight")
            else
                it.classList.add("highlight")
        }
        findWithValues(pattern)
    }

    private fun findWithValues(pattern: String): Map<String, VarInfo> {
        data.forEach { (key: String, _: VarInfo) ->
            val element = root.getElement<HTMLElement>(getIdForPath(key))
            if (pattern.isBlank() ||
                varInfo(key).value == null ||
                pattern.lowercase() !in toEditor(varInfo(key).type, varInfo(key).value).lowercase()
            )
                element.classList.remove("highlight")
            else
                element.classList.add("highlight")
        }
        return data
    }

    private fun expandCollapseAll(collapse: Boolean) {
        for (element in root.getElementsByClassName("collapsible")) {
            if (element.id == "object-") continue
            if (collapse)
                element.classList.add("collapsed")
            else
                element.classList.remove("collapsed")
        }
    }

    private fun varInfo(path: String): VarInfo =
        data[path] ?: error("variable not found in $path")

    private fun getInputType(type: String) = when (type) {
        "number" -> "number"
        "bigint" -> "number"
        else -> "text"
    }

    private fun fromEditor(type: String, value: Any?): Any =
        when (type) {
            "bigint" -> value.toString().toInt()
            "number" -> value.toString().toFloat()
            "boolean" -> value.toString().toBoolean()
            "string" -> value.toString()
            else -> value.toString()
        }

    private fun toEditor(type: String, value: Any?): String =
        when (type) {
            "bigint" -> value.toString()
            "number" -> value.toString()
            "boolean" -> value.toString().toBoolean().toString()
            "string" -> value.toString()
            else -> value.toString()
        }

    private fun toExpression(type: String, value: Any?): String =
        when (type) {
            "bigint" -> value.toString().toInt().toString()
            "number" -> value.toString().toFloat().toString()
            "string" -> "'${value.toString().replace("'", "\\'")}'"
            "boolean" -> value.toString().toBoolean().toString()
            else -> "'$value'"
        }

    private fun toBoolean(type: String, value: Any?): Boolean =
        when (type) {
            "bigint" -> value.toString().toInt() == 0
            "number" -> value.toString().toDouble() == 0.0
            "boolean" -> value.toString().toBoolean()
            "string" -> value.toString() in listOf("true", "1")
            else -> value.toString().toBoolean()
        }

    private fun initTools() {
        val root = root.div { id = "controls" }
        val table1 = root
            .table("grid tools")
        val tr1 = table1
            .tr("row")
        tr1
            .th("cell cell-label")
            .label("label").text(Messages.controls_filter_label())
        val elementFilter = tr1
            .td("cell cell-data")
            .input("text", "", "editor") {
                title = Messages.controls_filter_help()
                onKeyUp { filterSome(value) }
                onFocus { select() }
            }
        tr1
            .td("cell cell-data small")
            .input("button", Messages.controls_filter_button(), "button") {
                title = Messages.controls_filter_button_help()
                onClick {
                    elementFilter.value = ""
                    filterSome("")
                    elementFilter.focus()
                }
            }
        tr1
            .th("cell cell-label")
            .label("label").text(Messages.controls_highlight_label())
        val elementHighlight = tr1
            .td("cell cell-data")
            .input("text", "", "editor") {
                title = Messages.controls_highlight_help()
                onKeyUp { highlightSome(value) }
                onFocus { select() }
            }
        tr1
            .td("cell cell-data small")
            .input("button", Messages.controls_highlight_button(), "button") {
                title = Messages.controls_highlight_button_help()
                onClick {
                    elementHighlight.value = ""
                    highlightSome("")
                    elementHighlight.focus()
                }
            }
        val table2 = root
            .table("grid tools")
        val tr2 = table2
            .tr("row")
        tr2
            .th("cell cell-data")
            .label("label").text(Messages.controls_collapse_label())
        tr2
            .td("cell cell-data")
            .input("button", Messages.controls_collapse_button(), "button") {
                title = Messages.controls_collapse_button_help()
                onClick { expandCollapseAll(true) }
            }
        tr2
            .td("cell cell-data small")
        tr2
            .th("cell cell-data")
            .label("label").text(Messages.controls_expand_label())
        tr2
            .td("cell cell-data")
            .input("button", Messages.controls_expand_button(), "button") {
                title = Messages.controls_expand_button_help()
                onClick { expandCollapseAll(false) }
            }
        tr2
            .td("cell cell-data small")
        tr2
            .th("cell cell-data")
            .label("label").text(Messages.controls_unchange_label())
        tr2
            .td("cell cell-data")
            .input("button", Messages.controls_unchange_button(), "button") {
                title = Messages.controls_unchange_button_help()
                onClick { clearAllFieldsStyle() }
            }
        tr2
            .td("cell cell-data small")
    }

}
