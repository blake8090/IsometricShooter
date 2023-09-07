package bke.iso.engine.asset

import bke.iso.engine.Disposer
import bke.iso.engine.Game
import bke.iso.engine.Module
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Disposable
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

interface AssetLoader<T : Any> {
    fun load(file: File): T
}

private const val BASE_PATH = "assets"

class Assets(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}

    val fonts: Fonts = Fonts(this, game.renderer)

    private val loadersByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val assetCache = mutableMapOf<Pair<String, KClass<*>>, Any>()
    private val loadedAssets = mutableSetOf<Any>()

    override fun start() {
        addLoader("jpg", TextureLoader())
        addLoader("png", TextureLoader())
        addLoader("ttf", FreeTypeFontGeneratorLoader())
    }

    fun <T : Any> get(name: String, type: KClass<T>): T {
        val value = type.safeCast(assetCache[name to type])
        requireNotNull(value) {
            "No asset found for name '$name' and type '${type.simpleName}'"
        }
        return value
    }

    inline fun <reified T : Any> get(name: String): T =
        get(name, T::class)

    operator fun <T : Any> contains(asset: T) =
        if (asset is BitmapFont) {
            asset in fonts
        } else {
            loadedAssets.contains(asset)
        }

    fun addLoader(fileExtension: String, loader: AssetLoader<*>) {
        loadersByExtension[fileExtension]?.let { existing ->
            throw IllegalArgumentException(
                "Extension '$fileExtension' has already been set to loader ${existing::class.simpleName}"
            )
        }
        loadersByExtension[fileExtension] = loader
    }

    fun load(path: String) {
        val assetsPath = game.files.combinePaths(BASE_PATH, path)
        log.info { "Loading assets in path '$assetsPath'" }

        for (file in game.files.listFiles(assetsPath)) {
            val assetLoader = loadersByExtension[file.extension]
            if (assetLoader == null) {
                log.warn { "No loader found for '.${file.extension}', skipping file '${file.canonicalPath}'" }
                continue
            }
            load(file, assetLoader)
        }
    }

    private fun <T : Any> load(file: File, assetLoader: AssetLoader<T>) {
        val asset = assetLoader.load(file)
        val type = asset::class

        val parentPath = game.files.relativeParentPath(BASE_PATH, file)
        val name = game.files.combinePaths(parentPath, file.nameWithoutExtension)

        assetCache[name to type] = asset
        loadedAssets.add(asset)
        log.info { "Loaded asset '${name}' (${type::class.simpleName}) from '${file.canonicalPath}'" }
    }

    override fun dispose() {
        log.info { "Disposing assets" }
        for ((nameType, asset) in assetCache) {
            if (asset is Disposable) {
                Disposer.dispose(asset, nameType.first)
                loadedAssets.remove(asset)
            }
        }
        fonts.dispose()
    }
}
