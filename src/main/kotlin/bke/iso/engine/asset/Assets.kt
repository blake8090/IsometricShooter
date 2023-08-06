package bke.iso.engine.asset

import bke.iso.engine.Game
import bke.iso.engine.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import mu.KotlinLogging
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Asset<T>(
    val name: String,
    val value: T
)

private const val ASSETS_DIRECTORY = "assets"

class Assets(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    private val loadersByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val assets = mutableMapOf<Pair<String, KClass<*>>, Asset<*>>()

    private val fontCache = mutableMapOf<FontOptions, BitmapFont>()

    fun addLoader(fileExtension: String, loader: AssetLoader<*>) {
        val existingLoader = loadersByExtension[fileExtension]
        require(existingLoader == null) {
            "Extension '$fileExtension' has already been set to loader ${existingLoader!!::class.simpleName}"
        }
        loadersByExtension[fileExtension] = loader
    }

    fun <T : Any> get(name: String, type: KClass<T>): T {
        val asset = assets[name to type]?.value
        val value = type.safeCast(asset)
        requireNotNull(value) {
            "No asset found for name '$name' and type '${type.simpleName}'"
        }
        return value
    }

    inline fun <reified T : Any> get(name: String): T =
        get(name, T::class)

    fun getFont(name: String, dp: Float, color: Color = Color.WHITE): BitmapFont {
        val options = FontOptions(name, dp, color)
        val generator = get<FreeTypeFontGenerator>(name)
        return fontCache.getOrPut(options) {
            val pixels = dp * Gdx.graphics.density
            val parameter = FreeTypeFontParameter()
            parameter.size = pixels.toInt()
            parameter.color = color
            generator.generateFont(parameter)
        }
    }

    fun load(module: String) {
        log.info("Loading assets from module '$module'")
        val path = Path(ASSETS_DIRECTORY, module).toString()
        val files = game.fileSystem.getFiles(path)

        for (file in files) {
            val assetLoader = loadersByExtension[file.extension]
            if (assetLoader == null) {
                log.warn("No loader found for extension '${file.extension}' - skipping file ${file.path}")
                continue
            }
            load(file, assetLoader)
        }
    }

    private fun <T : Any> load(file: File, assetLoader: AssetLoader<T>) {
        val (name, asset) = assetLoader.load(file)
        set(name, Asset(name, asset))
        log.info("Loaded asset '${name}' (${asset::class.simpleName}) - '${file.path}'")
    }

    private fun <T : Any> set(name: String, asset: Asset<T>) {
        assets[name to asset.value::class] = asset
    }
}

private data class FontOptions(
    val name: String,
    val dp: Float,
    val color: Color = Color.WHITE
)

class FreeTypeFontGeneratorLoader() : AssetLoader<FreeTypeFontGenerator> {
    override fun load(file: File): Pair<String, FreeTypeFontGenerator> {
        val generator = FreeTypeFontGenerator(FileHandle(file))
        return file.nameWithoutExtension to generator
    }
}
