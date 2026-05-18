package bke.iso

import bke.iso.engine.Engine
import bke.iso.engine.core.Game
import bke.iso.engine.initImGui
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import java.io.File
import kotlin.system.exitProcess

@Serializable
data class AppConfig(
    val displaySettings: DisplaySettings,
    val vsyncEnabled: Boolean,
    val limitFPS: Boolean
)

@Serializable
enum class WindowMode {
    @SerialName("windowed")
    WINDOWED,

    @SerialName("fullscreen")
    FULLSCREEN,

    @SerialName("borderless")
    BORDERLESS
}

@Serializable
data class DisplaySettings(
    val windowMode: WindowMode,
    val resolutionWidth: Int,
    val resolutionHeight: Int
)

class App(private val game: Game) : ApplicationAdapter() {

    private val log = KotlinLogging.logger { }

    private lateinit var engine: Engine

    override fun create() {
        initImGui()
        KtxAsync.initiate()
        engine = Engine(game)
        engine.start()
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        engine.stop()
    }

    override fun resize(width: Int, height: Int) {
        engine.resize(width, height)
    }

    fun getConfig(): AppConfig {
        val format = Json {
            isLenient = true
            prettyPrint = true
        }

        val file = File("${game.gameDataPath}/app.cfg")

        val config: AppConfig =
            if (file.exists()) {
                format.decodeFromString<AppConfig>(file.readText())
            } else {
                log.info { "Config file not found, writing default: ${file.path}" }
                val defaultConfig = createDefaultConfig()
                file.createNewFile()
                file.writeText(format.encodeToString(defaultConfig))
                defaultConfig
            }

        return config
    }

    private fun createDefaultConfig() =
        AppConfig(
            displaySettings = DisplaySettings(
                windowMode = WindowMode.FULLSCREEN,
                resolutionWidth = Lwjgl3ApplicationConfiguration.getDisplayMode().width,
                resolutionHeight = Lwjgl3ApplicationConfiguration.getDisplayMode().height,
            ),
            vsyncEnabled = true,
            limitFPS = true
        )

    fun handleUncaughtException(e: Throwable) {
        log.error(e) { "Uncaught exception" }
        exitProcess(-1)
    }
}
