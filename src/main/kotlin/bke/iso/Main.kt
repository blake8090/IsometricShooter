package bke.iso

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application

fun main() {
    val app = App()
    Lwjgl3Application(app, app.config)
}
