package honeypot.api

import kotlin.js.Json

fun jsTypeOf(@Suppress("UNUSED_PARAMETER") o: Any?): String = js("typeof o") as String
fun <T> jsEval(@Suppress("UNUSED_PARAMETER") o: String): T? = js("eval(o)") as T?

fun jsIsJson(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    (js("o.constructor === ({}).constructor")) as Boolean

fun jsIsArray(@Suppress("UNUSED_PARAMETER") o: dynamic): Boolean =
    (js("o.constructor === [].constructor")) as Boolean

fun jsEntries(@Suppress("UNUSED_PARAMETER") o: dynamic): List<Pair<String, Any?>> =
    (js("Object.entries(o)") as? Array<Array<Any?>>)!!.map { it[0].toString() to it[1] }

fun jsKeys(@Suppress("UNUSED_PARAMETER") o: dynamic): List<String> =
    (js("Object.keys(o)") as? Array<String>)!!.toList()

fun jsValues(@Suppress("UNUSED_PARAMETER") o: dynamic): List<Any?> =
    (js("Object.values(o)") as? Array<Any>)!!.toList()

val Json.entries: List<Pair<String, Any?>>
    get() = jsEntries(this)
val Json.keys: List<String>
    get() = jsKeys(this)
val Json.values: List<Any?>
    get() = jsValues(this)
