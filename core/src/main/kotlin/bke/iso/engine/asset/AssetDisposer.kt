package bke.iso.engine.asset

import com.badlogic.gdx.utils.Disposable
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Keeps track of disposed objects to avoid `Class already disposed!` errors.
 */
class AssetDisposer {

    private val log = KotlinLogging.logger {}

    private val disposed = mutableSetOf<String>()

    fun <T : Disposable> dispose(name: String, disposable: T) {
        val className = disposable::class.simpleName

        if (disposed.add(name)) {
            disposable.dispose()
            log.debug { "Disposed '$name' ($className)" }
        } else {
            log.debug { "Asset '$name' ($className) has already been disposed" }
        }
    }
}
