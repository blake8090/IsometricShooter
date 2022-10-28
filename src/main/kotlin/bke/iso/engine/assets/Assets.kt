package bke.iso.engine.assets

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.FilePointer
import bke.iso.engine.Filesystem
import bke.iso.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Asset<T>(
    val name: String,
    val asset: T
)

typealias AssetMap<T> = MutableMap<String, Asset<T>>

@Service
class Assets(
    private val services: Services,
    private val filesystem: Filesystem
) {
    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val cacheByType = mutableMapOf<KClass<*>, AssetMap<*>>()

    fun <T : Any> addLoader(extension: String, loaderType: KClass<out AssetLoader<T>>) {
        if (loaderByExtension.containsKey(extension)) {
            throw IllegalArgumentException("duplicate extension")
        }
        loaderByExtension[extension] = services.createInstance(loaderType)
        log.debug("added asset loader '${loaderType.simpleName}' for extension '$extension'")
    }

    fun load(path: String) {
        log.info("Loading assets from path: '${filesystem.getCanonicalPath(path)}'")

        val filesByExtension = filesystem
            .getFiles(path)
            .groupBy(FilePointer::getExtension)

        for ((extension, files) in filesByExtension) {
            val loader = loaderByExtension[extension]
            if (loader != null) {
                log.info("Loading ${files.size} '.$extension' file(s) as type '${loader.getType().simpleName}'")
                loadAssets(loader, files)
            } else {
                log.warn("No asset loader for file extension '.$extension' was defined, skipping ${files.size} file(s)")
            }
        }
    }

    private fun <T : Any> loadAssets(loader: AssetLoader<T>, files: List<FilePointer>) {
        val cache = cacheByType.getOrPut(loader.getType()) { mutableMapOf() }
        loader.load(files).forEach(cache::set)
    }

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        val cache = cacheByType.getOrPut(type) { mutableMapOf() }
        val asset = cache[name] ?: return null
        return type.safeCast(asset.asset)
    }

    inline fun <reified T : Any> get(name: String): T? =
        get(name, T::class)
}
