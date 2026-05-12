package bke.iso.engine.core

interface Event

class Events(private val handler: (Event) -> Unit) {

    fun fire(event: Event) {
        handler.invoke(event)
    }
}
