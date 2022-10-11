package bke.iso.v2.engine.assets

import bke.iso.engine.di.ServiceContainer
import bke.iso.engine.util.FilePointer
import bke.iso.engine.util.FileService
import bke.iso.engine.util.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class Assets(
    private val container: ServiceContainer,
    private val fileService: FileService
) {
    private val log = getLogger()

    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val cacheByType = mutableMapOf<KClass<*>, AssetMap<*>>()

    fun <T : Any> addLoader(extension: String, loaderType: KClass<out AssetLoader<T>>) {
        if (loaderByExtension.containsKey(extension)) {
            throw IllegalArgumentException("duplicate extension")
        }
        loaderByExtension[extension] = container.createInstance(loaderType)
    }

    fun load(path: String) {
        fileService.getFiles(path)
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
