package bke.iso.engine.state

import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.EngineModule
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class States(private val engine: Engine) : EngineModule() {

    private val log = KotlinLogging.logger { }

    override val moduleName: String = "states"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    private var currentState: State = EmptyState(engine)

    override fun update(deltaTime: Float) {
        currentState.update(deltaTime)
    }

    override fun handleEvent(event: Event) {
        currentState.handleEvent(event)
    }

    fun <T : State> setState(type: KClass<T>) {
        log.debug { "Switching to state ${type.simpleName}" }
        currentState = requireNotNull(type.primaryConstructor).call(engine)
        // TODO: does state.load need to be a suspend fun anymore?
        runBlocking { currentState.load() }
    }

    inline fun <reified T : State> setState() =
        setState(T::class)
}
