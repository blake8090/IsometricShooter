package bke.iso

import bke.iso.v2.app.App
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application

fun main() {
    val app = App()
    Lwjgl3Application(app, app.buildConfig())
}
