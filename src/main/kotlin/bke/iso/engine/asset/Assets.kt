package bke.iso.engine.asset

import bke.iso.engine.Disposer
import bke.iso.engine.SystemInfo
import bke.iso.engine.asset.loader.AssetLoader
import bke.iso.engine.file.Files
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedMap.OrderedMapValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import ktx.async.KtxAsync
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KClass

const val BASE_PATH = "assets"

private typealias AssetCache<T> = OrderedMap<String, T>

// TODO: move this to Game or something
fun getCoroutineScope(): CoroutineScope =
    KtxAsync

// TODO: handle duplicate assets being loaded
class Assets(private val files: Files, systemInfo: SystemInfo) {

    private val log = KotlinLogging.logger {}

    val fonts: Fonts = Fonts(this, systemInfo)

    private val loaderByExtension = OrderedMap<String, AssetLoader<*>>()
    private val cacheByType = OrderedMap<KClass<*>, AssetCache<*>>()

    inline fun <reified T : Any> get(name: String): T =
        getCache(T::class)
            .get(name)
            ?: error("Asset '$name' (${T::class.simpleName}) was not found")

    inline fun <reified T : Any> getAll(): OrderedMapValues<T> =
        OrderedMapValues(getCache(T::class))

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getCache(type: KClass<T>): AssetCache<T> =
        cacheByType[type] as? AssetCache<T>
            ?: error("Expected asset cache for type ${type.simpleName}")

    fun register(assetLoader: AssetLoader<*>) {
        for (extension in assetLoader.extensions) {
            validateLoader(extension, assetLoader)
            loaderByExtension.put(extension, assetLoader)
        }
    }

    operator fun <T : Any> contains(asset: T) =
        if (asset is BitmapFont) {
            asset in fonts
        } else if (!cacheByType.containsKey(asset::class)) {
            false
        } else {
            getCache(asset::class).containsValue(asset, false)
        }

    private fun validateLoader(extension: String, assetLoader: AssetLoader<*>) {
        val existing = loaderByExtension[extension]
        if (existing != null) {
            val message = "Error registering ${assetLoader::class.simpleName}: " +
                    "Extension '$extension' already registered to ${existing::class.simpleName}"
            error(message)
        }
    }

    suspend fun loadAsync(path: String) = coroutineScope {
        val assetsPath = files.combinePaths(BASE_PATH, path)
        log.info { "Loading assets in path '$assetsPath'" }

        val files = withContext(Dispatchers.IO) {
            files.listFiles(assetsPath)
        }

        for (file in files) {
            load(file, loaderByExtension[file.extension])
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> load(file: File, assetLoader: AssetLoader<T>?) {
        if (assetLoader == null) {
            log.warn { "Skipping file skipping file '${file.canonicalPath}': No loader found for '${file.extension}'" }
            return
        }

        val name = file.name
        val asset = assetLoader.load(file)

        val type = asset::class as KClass<T>
        if (!cacheByType.containsKey(type)) {
            cacheByType.put(type, AssetCache<T>())
        }

        getCache(type).put(name, asset)
        log.info { "Loaded asset '${name}' (${type.simpleName}) from '${file.canonicalPath}'" }
    }

    fun dispose() {
        log.info { "Disposing assets" }
        for ((_, cache) in cacheByType) {
            disposeCache(cache)
        }
        fonts.dispose()
    }

    private fun disposeCache(cache: AssetCache<*>) {
        for ((name, asset) in cache) {
            if (asset is Disposable) {
                Disposer.dispose(asset, name)
            }
        }
    }
}

private operator fun <K, V> ObjectMap.Entry<K, V>.component1(): K =
    key

private operator fun <K, V> ObjectMap.Entry<K, V>.component2(): V =
    value
