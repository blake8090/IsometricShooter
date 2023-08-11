package bke.iso.engine

import bke.iso.engine.ui.UIScreen
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ktx.async.KtxAsync
import mu.KotlinLogging

// TODO: move into GameState
class Loader {

    private val log = KotlinLogging.logger {}

    var isLoading = false
        private set

    var screen: UIScreen? = null
    var onLoad: () -> Unit = {}
    var onFinish: () -> Unit = {}

    fun start() {
        isLoading = true
        KtxAsync.async {
            if (screen != null) {
                // give enough time to display loading screen
                delay(300)
            }

            log.debug { "begin loading" }
            onLoad.invoke()
            log.debug { "end loading" }

            onFinish.invoke()
            isLoading = false
            log.debug { "finished loading" }
        }
    }
}
