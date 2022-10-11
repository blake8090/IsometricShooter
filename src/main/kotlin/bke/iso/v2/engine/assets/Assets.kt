package bke.iso.v2.engine.assets

import bke.iso.engine.util.getLogger
import bke.iso.v2.app.service.Service
import bke.iso.v2.app.service.Services
import bke.iso.v2.engine.FilePointer
import bke.iso.v2.engine.Filesystem
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

@Service
class Assets(
    private val services: Services,
    private val filesystem: Filesystem
) {
    private val log = getLogger()

    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val cacheByType = mutableMapOf<KClass<*>, AssetMap<*>>()

    fun <T : Any> addLoader(extension: String, loaderType: KClass<out AssetLoader<T>>) {
        if (loaderByExtension.containsKey(extension)) {
            throw IllegalArgumentException("duplicate extension")
        }
        loaderByExtension[extension] = services.createInstance(loaderType)
    }

    fun load(path: String) {
        filesystem.getFiles(path)
            .groupBy(FilePointer::getExtension)
            .forEach { (extension, files) ->
                val loader = loaderByExtension[extension]
                    ?: throw IllegalArgumentException("No loader for file type '$extension' was defined")
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
