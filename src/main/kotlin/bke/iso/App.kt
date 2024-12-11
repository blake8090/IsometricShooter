package bke.iso

import bke.iso.engine.Game
import bke.iso.engine.GameInfo
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.async.KtxAsync
import java.io.File

@Serializable
data class AppConfig(
    val vsyncEnabled: Boolean,
    val windowWidth: Int,
    val windowHeight: Int,
    val fullscreen: Boolean
)

class App(private val gameInfo: GameInfo) : ApplicationAdapter() {

    private val log = KotlinLogging.logger { }

    val paths = AppPaths(gameInfo)

    private lateinit var game: Game

    override fun create() {
        KtxAsync.initiate()
        game = Game(paths)
        game.start(gameInfo)
    }

    override fun render() {
        game.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        game.stop()
    }

    override fun resize(width: Int, height: Int) {
        game.resize(width, height)
    }

    fun getConfig(): Lwjgl3ApplicationConfiguration {
        val format = Json {
            isLenient = true
            prettyPrint = true
        }

        val file = File("${paths.getGameDataPath()}/app.cfg")

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

        return Lwjgl3ApplicationConfiguration().apply {
            setTitle(gameInfo.windowTitle)
            useVsync(config.vsyncEnabled)

            if (config.fullscreen) {
                setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode())
            } else {
                setWindowedMode(config.windowWidth, config.windowHeight)
            }
        }
    }

    private fun createDefaultConfig(): AppConfig {
        return AppConfig(
            true,
            Lwjgl3ApplicationConfiguration.getDisplayMode().width,
            Lwjgl3ApplicationConfiguration.getDisplayMode().height,
            true
        )
    }
}
