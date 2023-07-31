package bke.iso.v2

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application

fun main() {
    val app = App("Isometric Shooter")
    Lwjgl3Application(app, app.config)
}
