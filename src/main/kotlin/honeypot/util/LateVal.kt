package honeypot.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LateVal<T : Any> : ReadWriteProperty<Any, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        value ?: error("Value isn't initialized")

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        check(this.value == null) { "Value is already initialized" }
        this.value = value
    }
}
