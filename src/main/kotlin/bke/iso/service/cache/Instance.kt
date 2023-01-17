package bke.iso.service.cache

import bke.iso.service.PostInit
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

internal class Instance<T : Any>(val value: T) {

    private var state = State.CREATED

    fun callPostInit() {
        if (state == State.POST_INIT || state == State.DONE) {
            return
        }

        value::class.functions
            .firstOrNull { func -> func.hasAnnotation<PostInit>() }
            ?.call(value)

        state = State.DONE
    }
}

private enum class State {
    CREATED,
    POST_INIT,
    DONE
}
