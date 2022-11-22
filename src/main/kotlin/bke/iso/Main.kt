package bke.iso

import bke.iso.app.App
import bke.iso.game.IsometricShooterGame
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application

fun main() {
    val app = App(IsometricShooterGame::class)
    Lwjgl3Application(app, app.buildConfig())
}
