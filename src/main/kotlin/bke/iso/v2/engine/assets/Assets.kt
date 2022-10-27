package bke.iso.v2.engine.assets

import bke.iso.v2.app.service.Service
import bke.iso.v2.app.service.Services
import bke.iso.v2.engine.FilePointer
import bke.iso.v2.engine.Filesystem
import bke.iso.v2.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

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

        filesystem.getFiles(path)
            .groupBy(FilePointer::getExtension)
            .forEach(this::loadFilesByExtension)
    }

    private fun loadFilesByExtension(extension: String, files: List<FilePointer>) {
        val loader = loaderByExtension[extension]
        if (loader == null) {
            log.warn("No asset loader for file extension '$extension' was defined, skipping ${files.size} files")
            return
        }

        files.forEach {
            log.trace("loading file: '${it.getPath()}'")
            loadAssets(loader, files)
        }
    }

    private fun <T : Any> loadAssets(loader: AssetLoader<T>, files: List<FilePointer>) {
        val cache = cacheByType.getOrPut(loader.getType()) { mutableMapOf() }
        loader.load(files)
            .forEach { (name, asset) ->
                cache[name] = asset
                log.info("Loaded asset '${asset.name}' of type '${loader.getType().simpleName}'")
            }
    }

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        val cache = cacheByType.getOrPut(type) { mutableMapOf() }
        val asset = cache[name]
        return type.safeCast(asset)
    }

    inline fun <reified T : Any> get(name: String): T? =
        get(name, T::class)
}
