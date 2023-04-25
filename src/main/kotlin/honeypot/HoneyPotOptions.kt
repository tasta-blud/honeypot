package honeypot

import honeypot.api.*
import honeypot.css.Stylesheets
import honeypot.data.Options
import kotlinx.browser.window
import org.w3c.dom.Element

@OptIn(ExperimentalJsExport::class)
@JsExport
class HoneyPotOptions(
    private val root: Element = window.document.getElementById("root") ?: error("no root element"),
    private val callback: () -> Unit = {}
) {
    private val options = Options()
    val automatic: Boolean
        get() = options.automatic
    val interval: Int
        get() = options.interval

    init {
        root.addStyle(Stylesheets.stylesheet)
        root.innerHTML = ""
        initHeader()
        initOptions()
    }

    private fun initHeader() {
        val manifest = getManifest()
        root.table("grid tools").tr("row") {
            td("cell cell-center") {
                a(manifest.getString("homepage_url")) {
                    h2("${manifest["name"]} v.${manifest["version"]}")
                    target = "_blank"
                }
            }
        }
    }

    private fun initOptions() {
        val table = root
            .table("grid tools")
        table
            .caption("caption").text(Messages.options_caption())
        val tr1 = table
            .tr("row")
        tr1
            .th("cell cell-label")
            .label("label").text(Messages.options_automatic_label())
        val elementAutomatic = tr1
            .td("cell cell-data")
            .input("checkbox", "", "editor") {
                checked = options.automatic
                onClick {
                    options.automatic = checked
                    save(options)
                    if (options.automatic) callback()
                }
            }
        tr1
            .td("cell cell-data small").text(Messages.options_automatic_help())
        tr1
            .th("cell cell-label")
            .label("label").text(Messages.options_interval_label())
        val elementInterval = tr1
            .td("cell cell-data")
            .input("number", options.interval.toString(), "editor") {
                onChange {
                    options.interval = value.toInt()
                    save(options)
                }
            }
        tr1
            .td("cell cell-data small").text(Messages.options_interval_help())
        load(options) {
            if (it.automatic) options.automatic = it.automatic
            if (it.interval != 0) options.interval = it.interval
            elementAutomatic.checked = options.automatic
            elementInterval.value = options.interval.toString()
            if (it.automatic) callback()
        }
    }


    private fun save(options: Options) =
        storageSet(options) { alert(Messages.options_error_save(), it) }


    private fun load(options: Options, onOptions: (Options) -> Unit) =
        storageGet(options, onOptions) { alert(Messages.options_error_load(), it) }

}
