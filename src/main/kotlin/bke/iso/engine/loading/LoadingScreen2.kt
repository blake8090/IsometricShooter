package bke.iso.engine.loading

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.skipFrame
import kotlin.system.measureTimeMillis

/**
 * A [LoadingScreen2] represents a loading sequence consisting of 4 states:
 * - Transition In
 * - Loading
 * - Transition Out
 * - Idle
 *
 *
 */
abstract class LoadingScreen2 {

    private val log = KotlinLogging.logger { }

    var active: Boolean = false
        private set

    protected abstract val transitionInState: TransitionInState
    protected abstract val loadingState: LoadingState
    protected abstract val transitionOutState: TransitionOutState
    private val idleState = object : IdleState() {
        override fun start() {}

        override fun update(deltaTime: Float) {}
    }
    private var currentState: LoadingScreenState = idleState

    private var action: suspend () -> Unit = {}
    private var actionStarted = false
    private var actionCompleted = false

    /**
     * Starts the loading screen...
     * The [action] will be invoked in the next few frames when the screen finishes its [TransitionInState].
     */
    fun start(action: suspend () -> Unit) {
        this.action = action
        nextState()
        active = true
        log.debug { "Loading screen started" }
    }

    /**
     * Stops the loading screen.
     */
    fun stop() {
        currentState = idleState
        active = false
    }

    /**
     * Updates the screen if it is active.
     */
    fun update(deltaTime: Float) {
        if (!active) {
            return
        }

        currentState.update(deltaTime)

        if (currentState is LoadingState) {
            if (!actionStarted) {
                startAction()
            } else if (actionCompleted) {
                nextState()
            }
        }
    }

    private fun startAction() {
        KtxAsync.launch {
            // lets the loading screen display first before starting action
            skipFrame()
            val time = measureTimeMillis { action.invoke() }
            log.debug { "Load action completed in $time ms" }
            actionCompleted = true
        }

        log.debug { "Launched load action" }
        actionStarted = true
    }

    protected fun nextState() {
        when (currentState) {
            is IdleState -> {
                log.debug { "Switching from Idle to TransitionIn" }
                currentState = transitionInState
                transitionInState.start()
            }

            is TransitionInState -> {
                log.debug { "Switching from TransitionIn to Loading" }
                currentState = loadingState
                loadingState.start()
            }

            is LoadingState -> {
                log.debug { "Switching from Loading to TransitionOut" }
                currentState = transitionOutState
                transitionOutState.start()
            }

            is TransitionOutState -> {
                log.debug { "Finished TransitionOut, stopping" }
                stop()
            }
        }
    }

    private sealed interface LoadingScreenState {
        fun start()
        fun update(deltaTime: Float)
    }

    protected abstract class IdleState : LoadingScreenState
    protected abstract class TransitionInState : LoadingScreenState
    protected abstract class LoadingState : LoadingScreenState
    protected abstract class TransitionOutState : LoadingScreenState
}
