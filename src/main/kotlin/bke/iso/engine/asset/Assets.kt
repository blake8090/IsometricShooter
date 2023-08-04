package bke.iso.engine.asset

import bke.iso.engine.Game
import bke.iso.engine.Module
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

class Assets(override val game: bke.iso.engine.Game) : bke.iso.engine.Module() {

    private val log = KotlinLogging.logger {}

    private val loadersByExtension = mutableMapOf<String, bke.iso.engine.asset.AssetLoader<*>>()
    private val assets = mutableMapOf<Pair<String, KClass<*>>, bke.iso.engine.asset.Asset<*>>()

    fun addLoader(fileExtension: String, loader: bke.iso.engine.asset.AssetLoader<*>) {
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

    fun load(module: String) {
        log.info("Loading assets from module '$module'")
        val path = Path(bke.iso.engine.asset.ASSETS_DIRECTORY, module).toString()
        val files = game.fileSystem.getFiles(path)

        for (file in files) {
            val assetLoader = loadersByExtension[file.extension]
            if (assetLoader == null) {
                log.warn("No loader found for extension '${file.extension}' - skipping file ${file.getPath()}")
                continue
            }
            load(file, assetLoader)
        }
    }

    private fun <T : Any> load(file: File, assetLoader: bke.iso.engine.asset.AssetLoader<T>) {
        val (name, asset) = assetLoader.load(file)
        set(name, bke.iso.engine.asset.Asset(name, asset))
        log.info("Loaded asset '${name}' (${asset::class.simpleName}) - '${file.path}'")
    }

    private fun <T : Any> set(name: String, asset: bke.iso.engine.asset.Asset<T>) {
        assets[name to asset.value::class] = asset
    }
}
