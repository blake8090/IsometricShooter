package bke.iso.engine.asset

import bke.iso.engine.SystemInfo
import bke.iso.engine.asset.cache.AssetCache
import bke.iso.engine.os.Files
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.OrderedMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KClass

const val BASE_PATH = "assets"

class Assets(private val files: Files, systemInfo: SystemInfo) {

    private val log = KotlinLogging.logger {}

    val fonts: Fonts = Fonts(this, systemInfo)

    private val cacheByExtension = OrderedMap<String, AssetCache<*>>()
    private val cacheByType = OrderedMap<KClass<*>, AssetCache<*>>()

    fun <T : Any> register(assetType: KClass<T>, assetCache: AssetCache<T>) {
        for (extension in assetCache.extensions) {
            cacheByExtension.put(extension, assetCache)?.let { existing ->
                error("Extension '$extension' already registered to ${existing::class.simpleName}")
            }
        }
        cacheByType.put(assetType, assetCache)
    }

    inline fun <reified T : Any> register(assetCache: AssetCache<T>) =
        register(T::class, assetCache)

    fun <T : Any> get(name: String, type: KClass<T>): T =
        getCache(type)[name] ?: error("Asset not found: '$name' (${type.simpleName})")

    inline fun <reified T : Any> get(name: String): T =
        get(name, T::class)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> contains(asset: T) =
        if (asset is BitmapFont) {
            asset in fonts
        } else {
            tryGetCache(asset::class as KClass<T>)
                ?.contains(asset)
                ?: false
        }

    fun <T : Any> getAll(type: KClass<T>): List<T> =
        getCache(type).getAll()

    inline fun <reified T : Any> getAll() =
        getAll(T::class)

    suspend fun loadAsync(path: String) {
        val assetsPath = files.combinePaths(BASE_PATH, path)
        log.info { "Loading assets in path '$assetsPath'" }

        val files = withContext(Dispatchers.IO) {
            files.listFiles(assetsPath)
        }

        val fileToCache = files.associateWith { file -> cacheByExtension[file.extension] }
        for ((file, cache) in fileToCache) {
            loadFileAsync(file, cache)
        }
    }

    private suspend fun loadFileAsync(file: File, cache: AssetCache<*>?) {
        if (cache == null) {
            log.info { "Skipping '${file.path}': Unknown extension '${file.extension}'" }
            return
        }
        try {
            for ((name, asset) in cache.load(file)) {
                log.info { "Loaded asset '${name}' (${asset::class.simpleName}) from '${file.canonicalPath}'" }
            }
        } catch (e: Throwable) {
            log.error("Error loading asset from '${file.canonicalPath}':", e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> tryGetCache(type: KClass<T>): AssetCache<T>? =
        cacheByType[type] as? AssetCache<T>

    private fun <T : Any> getCache(type: KClass<T>): AssetCache<T> =
        checkNotNull(tryGetCache(type)) {
            "Expected asset cache for type ${type.simpleName}"
        }

    fun dispose() {
        fonts.dispose()
        for (cache in cacheByType.values()) {
            cache.dispose()
        }
    }
}
