package bke.iso.engine.asset

import bke.iso.engine.Disposer
import bke.iso.engine.SystemInfo
import bke.iso.engine.file.Files
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedMap.OrderedMapValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ktx.async.KtxAsync
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KClass

interface AssetLoader<T : Any> {
    val extensions: List<String>
        get() = emptyList() //TODO sdf

    fun load(file: File): T
}

const val BASE_PATH = "assets"

private typealias AssetCache<T> = OrderedMap<String, T>

// TODO: move this to Game or something
fun getCoroutineScope(): CoroutineScope =
    KtxAsync

class Assets(private val files: Files, systemInfo: SystemInfo) {

    private val log = KotlinLogging.logger {}

    val fonts: Fonts = Fonts(this, systemInfo)

    private val loaderByExtension = OrderedMap<String, AssetLoader<*>>()

    private val cacheByType = OrderedMap<KClass<*>, AssetCache<*>>()
    private val cacheMutex = Mutex()

    inline fun <reified T : Any> get(name: String): T =
        getCache<T>()
            .get(name)
            ?: error("Asset '$name' (${T::class.simpleName}) was not found")

    inline fun <reified T : Any> getAll(): OrderedMapValues<T> =
        OrderedMapValues(getCache<T>())

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getCache(type: KClass<T>): AssetCache<T> =
        cacheByType[type] as? AssetCache<T>
            ?: error("Expected asset cache for type ${type.simpleName}")

    inline fun <reified T : Any> getCache(): AssetCache<T> =
        getCache(T::class)

    fun register(assetLoader: AssetLoader<*>) {
        for (extension in assetLoader.extensions) {
            validateLoader(extension, assetLoader)
            loaderByExtension.put(extension, assetLoader)
        }
    }

    inline operator fun <reified T : Any> contains(asset: T) =
        if (asset is BitmapFont) {
            asset in fonts
        } else {
            getCache<T>().containsValue(asset, false)
        }

    private fun validateLoader(extension: String, assetLoader: AssetLoader<*>) {
        val existing = loaderByExtension[extension]
        if (existing != null) {
            val message = "Error registering ${assetLoader::class.simpleName}: " +
                    "Extension '$extension' already registered to ${existing::class.simpleName}"
            error(message)
        }
    }

    fun load(string: String) {}

    // TODO: figure out how to unit test this
    suspend fun loadAsync(path: String) {
        val assetsPath = files.combinePaths(BASE_PATH, path)
        log.info { "Loading assets in path '$assetsPath'" }

        val files = withContext(Dispatchers.IO) {
            files.listFiles(assetsPath)
        }

        val tasks = files
            .mapTo(mutableListOf()) { file ->
                getCoroutineScope().async {
                    load(file, loaderByExtension[file.extension])
                }
            }
        tasks.awaitAll()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <T : Any> load(file: File, assetLoader: AssetLoader<T>?) {
        if (assetLoader == null) {
            log.warn { "Skipping file skipping file '${file.canonicalPath}': No loader found for '${file.extension}'" }
            return
        }

        val name = file.nameWithoutExtension
        val asset = assetLoader.load(file)

        cacheMutex.withLock {
            val type = asset::class as KClass<T>
            if (!cacheByType.containsKey(type)) {
                cacheByType.put(type, AssetCache<T>())
            }

            getCache(type).put(name, asset)
            log.info { "Loaded asset '${name}' (${type.simpleName}) from '${file.canonicalPath}'" }
        }
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

//    private val loadersByExtension = mutableMapOf<String, AssetLoader<*>>()
//    private val assetCache = mutableMapOf<Pair<String, KClass<*>>, Any>()
//    private val loadedAssets = mutableSetOf<Any>()
//
//    fun start() {
//        addLoader("jpg", TextureLoader())
//        addLoader("png", TextureLoader())
//        addLoader("ttf", FreeTypeFontGeneratorLoader())
//        addLoader("actor", ActorPrefabLoader(serializer))
//    }
//
//    fun <T : Any> get(name: String, type: KClass<T>): T {
//        val value = type.safeCast(assetCache[name to type])
//        requireNotNull(value) {
//            "No asset found for name '$name' and type '${type.simpleName}'"
//        }
//        return value
//    }
//
//    inline fun <reified T : Any> get(name: String): T =
//        get(name, T::class)
//

//        assetCache
//            .filterKeys { (_, assetType) -> assetType == type }
//            .values
//            .map(type::cast)
//
//    operator fun <T : Any> contains(asset: T) =
//        loadedAssets.contains(asset)
//
//    fun addLoader(fileExtension: String, loader: AssetLoader<*>) {
//        loadersByExtension[fileExtension]?.let { existing ->
//            throw IllegalArgumentException(
//                "Extension '$fileExtension' has already been set to loader ${existing::class.simpleName}"
//            )
//        }
//        loadersByExtension[fileExtension] = loader
//    }
//
//    fun load(path: String) {
//        val assetsPath = files.combinePaths(BASE_PATH, path)
//        log.info { "Loading assets in path '$assetsPath'" }
//
//        for (file in files.listFiles(assetsPath)) {
//            val assetLoader = loadersByExtension[file.extension]
//            if (assetLoader == null) {
//                log.warn { "No loader found for '.${file.extension}', skipping file '${file.canonicalPath}'" }
//                continue
//            }
//            load(file, assetLoader)
//        }
//    }
//
//    private fun <T : Any> load(file: File, assetLoader: AssetLoader<T>) {
//        val asset = assetLoader.load(file)
//        val type = asset::class
//
//        val parentPath = files.relativeParentPath(BASE_PATH, file)
//        val name = files.combinePaths(parentPath, file.nameWithoutExtension)
//
//        assetCache[name to type] = asset
//        loadedAssets.add(asset)
//        log.info { "Loaded asset '${name}' (${type.simpleName}) from '${file.canonicalPath}'" }
//    }
//
//    fun dispose() {
//        log.info { "Disposing assets" }
//        for ((nameType, asset) in assetCache) {
//            if (asset is Disposable) {
//                Disposer.dispose(asset, nameType.first)
//                loadedAssets.remove(asset)
//            }
//        }
//        fonts.dispose()
//    }
//}
