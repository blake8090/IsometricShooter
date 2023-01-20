package bke.iso.service.container

import bke.iso.engine.log
import bke.iso.service.PostInit
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

// TODO: rename to ServiceInstance
internal class Instance<T : Any>(val value: T, private val dependencies: List<Instance<*>>) {

    private var state = State.CREATED

    fun callPostInit() {
        if (state == State.POST_INIT || state == State.DONE) {
            return
        }

        state = State.POST_INIT

        dependencies.forEach { instance -> instance.callPostInit() }
        val func = findPostInit()
        if (func != null) {
            log.debug("Instance '${value::class.simpleName}' - calling PostInit")
            func.call(value)
        }

        state = State.DONE
    }

    private fun findPostInit(): KFunction<*>? {
        val func = value::class.functions
            .firstOrNull { func -> func.hasAnnotation<PostInit>() }
            ?: return null

        // TODO: validate function parameters
        return func
    }
}

private enum class State {
    CREATED,
    POST_INIT,
    DONE
}
