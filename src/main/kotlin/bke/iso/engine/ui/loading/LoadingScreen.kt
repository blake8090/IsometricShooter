package bke.iso.engine.ui.loading

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIScreen
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import ktx.async.skipFrame
import kotlin.system.measureTimeMillis

abstract class LoadingScreen(assets: Assets) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    var active: Boolean = false
        private set

    private var action: suspend () -> Unit = {}
    private var actionStarted = false

    fun start(action: suspend () -> Unit) {
        this.action = action
        active = true
        actionStarted = false
    }

    override fun draw(deltaTime: Float) {
        super.draw(deltaTime)

        if (active && !actionStarted) {
            KtxAsync.launch {
                // lets the loading screen display first before starting action
                skipFrame()

                val time = measureTimeMillis { action.invoke() }
                log.debug { "Load action completed in $time ms" }

                active = false
            }
            log.debug { "Launched load action" }
            actionStarted = true
        }
    }
}
