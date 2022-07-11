package bke.iso

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App : ApplicationAdapter() {
    private val engine = Engine()

    override fun create() {
        KtxAsync.initiate()
        engine.start()
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        engine.stop()
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration =
        Lwjgl3ApplicationConfiguration().apply {
            val config = engine.container.getService<ConfigService>().resolveConfig()
            setWindowedMode(config.width, config.height)
        }
}

fun main() {
    val app = App()
    Lwjgl3Application(app, app.buildConfig())
}
