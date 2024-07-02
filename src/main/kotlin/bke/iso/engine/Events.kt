package bke.iso.engine

interface Event

class Events(private val handler: (Event) -> Unit) {

    fun fire(event: Event) {
        handler.invoke(event)
    }
}
