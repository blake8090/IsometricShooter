package bke.iso

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val engine = Engine()

    Lwjgl3Application(
        object : ApplicationAdapter() {
            override fun create() {
                engine.start()
            }

            override fun render() {
                engine.update(Gdx.graphics.deltaTime)
            }

            override fun dispose() {
                engine.stop()
            }
        }, Lwjgl3ApplicationConfiguration()
    )
}
