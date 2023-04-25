package honeypot.data
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Options(var automatic: Boolean = true, var interval: Int = 500)
