package bke.iso

import bke.iso.app.App
import bke.iso.game.IsometricShooter
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application

fun main() {
    val app = App(IsometricShooter())
    Lwjgl3Application(app, app.buildConfig())
}
