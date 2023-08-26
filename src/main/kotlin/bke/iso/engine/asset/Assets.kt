package bke.iso.engine.asset

import bke.iso.engine.Disposer
import bke.iso.engine.Game
import bke.iso.engine.Module
import com.badlogic.gdx.utils.Disposable
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

    val fonts: Fonts = Fonts(this)

    private val loadersByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val assets = mutableMapOf<Pair<String, KClass<*>>, Asset<*>>()

    override fun start() {
        addLoader("jpg", TextureLoader())
        addLoader("png", TextureLoader())
        addLoader("ttf", FreeTypeFontGeneratorLoader())
    }

    override fun dispose() {
        log.info { "Disposing assets" }
        for (asset in assets.values) {
            if (asset.value is Disposable) {
                Disposer.dispose(asset.value, asset.name)
            }
        }
        fonts.dispose()
    }

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
