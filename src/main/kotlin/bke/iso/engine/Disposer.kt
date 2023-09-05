package bke.iso.engine

import com.badlogic.gdx.utils.Disposable
import mu.KotlinLogging

// TODO: make this a dependency instead of an object to facilitate unit testing?
/**
 * Keeps track of disposed objects to avoid `Class already disposed!` errors.
 */
object Disposer {

    private val log = KotlinLogging.logger {}

    private val disposed = mutableSetOf<Disposable>()

    fun <T : Disposable> dispose(disposable: T, disposableName: String? = null) {
        val name = disposableName ?: disposable.toString()
        val className = disposable::class.simpleName

        if (!disposed.add(disposable)) {
            log.debug { "Disposable $className: '$name' has already been disposed" }
            return
        }

        disposable.dispose()
        log.debug { "Disposed $className: '$name'" }
    }
}
