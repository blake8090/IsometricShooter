package bke.iso.v2.app

import bke.iso.v2.app.service.ServiceScanner
import bke.iso.v2.app.service.Services
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App : ApplicationAdapter() {
    private val services = Services()

    init {
        ServiceScanner()
            .scanClasspath("bke.iso")
            .forEach { javaClass -> services.register(javaClass.kotlin) }
    }

    override fun create() {
        KtxAsync.initiate()
    }

    override fun render() {
        super.render()
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("Title")
        config.useVsync(false)
        // TODO: load this from application config
        config.setWindowedMode(800, 600)
        return config
    }
}
