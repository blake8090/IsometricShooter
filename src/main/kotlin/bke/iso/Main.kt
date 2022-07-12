package bke.iso

import bke.iso.di.ServiceContainer
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

fun main() {
    // TODO: minor - convert this to a one-liner
    val container = ServiceContainer()
    container.registerFromClassPath("bke.iso")
    val config = container.getService<ConfigService>().resolveConfig()
    val engine = container.getService<Engine>()

    Lwjgl3Application(
        object : ApplicationAdapter() {
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
        },
        Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(config.width, config.height)
        }
    )
}
