package bke.iso.engine.asset

import bke.iso.engine.os.SystemInfo
import bke.iso.engine.asset.font.Fonts
import bke.iso.engine.asset.shader.Shaders
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
    val shaders: Shaders = Shaders(this)

    private val cacheByExtension = OrderedMap<String, AssetCache<*>>()
    private val cacheByType = OrderedMap<KClass<*>, AssetCache<*>>()
    private val assetDisposer = AssetDisposer()

    fun <T : Any> addCache(assetType: KClass<T>, assetCache: AssetCache<T>) {
        for (extension in assetCache.extensions) {
            addCache(extension, assetCache)
        }
        cacheByType.put(assetType, assetCache)
    }

    inline fun <reified T : Any> addCache(assetCache: AssetCache<T>) =
        addCache(T::class, assetCache)

    private fun <T : Any> addCache(extension: String, assetCache: AssetCache<T>) {
        val existing = cacheByExtension[extension]
        if (existing != null) {
            error("Extension '$extension' already registered to ${existing::class.simpleName}")
        }
        cacheByExtension.put(extension, assetCache)
    }

    fun <T : Any> get(path: String, type: KClass<T>): T =
        checkNotNull(getCache(type).get(path)) {
            "Asset not found: '$path' (${type.simpleName})"
        }

    inline fun <reified T : Any> get(name: String): T =
        get(name, T::class)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> contains(asset: T): Boolean {
        if (asset is BitmapFont) {
            return asset in fonts
        }

        val cache = cacheByType[asset::class] as? AssetCache<T>
        return cache
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

        val allFiles = withContext(Dispatchers.IO) {
            files.listFiles(assetsPath)
        }

        for (file in allFiles) {
            loadFileAsync(file)
        }
    }

    private suspend fun loadFileAsync(file: File) {
        val cache = cacheByExtension[file.extension]
        if (cache == null) {
            log.info { "Skipping '${file.canonicalPath}': Unknown extension '${file.extension}'" }
            return
        }

        try {
            cache.load(file)
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
        fonts.dispose(assetDisposer)
        for (cache in cacheByType.values()) {
            cache.dispose(assetDisposer)
        }
    }
}
