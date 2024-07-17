package bke.iso.engine.state

import bke.iso.engine.Game
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class States(private val game: Game) {

    private val log = KotlinLogging.logger { }

    var currentState: State = EmptyState(game)
        private set

    fun <T : State> setState(type: KClass<T>) {
        log.debug { "Switching to state ${type.simpleName}" }
        currentState = requireNotNull(type.primaryConstructor).call(game)
        // TODO: does state.load need to be a suspend fun anymore?
        runBlocking { currentState.load() }
    }

    inline fun <reified T : State> setState() =
        setState(T::class)
}
