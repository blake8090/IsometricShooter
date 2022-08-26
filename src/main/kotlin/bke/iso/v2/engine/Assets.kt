package bke.iso.v2.engine

import bke.iso.engine.di.ServiceContainer
import bke.iso.engine.util.FilePointer
import bke.iso.engine.util.FileService
import bke.iso.engine.util.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Asset<T>(
    val name: String,
    val asset: T
)

typealias AssetMap<T> = MutableMap<String, Asset<T>>

abstract class AssetLoader<T : Any> {
    abstract fun load(file: FilePointer): Asset<T>
    abstract fun validate(asset: Asset<T>, loadedAssets: AssetMap<T>): Boolean
    abstract fun getType(): KClass<T>

    fun load(files: List<FilePointer>): Map<String, Asset<T>> {
        val loadedAssets: AssetMap<T> = mutableMapOf()

        for (file in files) {
            val asset = load(file)
            if (validate(asset, loadedAssets)) {
                loadedAssets[asset.name] = asset
            }
        }

        return loadedAssets
    }
}

class Assets(
    private val container: ServiceContainer,
    private val fileService: FileService
) {
    private val log = getLogger()
    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()
    private val cacheByType = mutableMapOf<KClass<*>, AssetMap<*>>()

    fun <T : Any> addLoader(extension: String, type: KClass<T>, loaderType: KClass<out AssetLoader<T>>) {
        if (loaderByExtension.containsKey(extension)) {
            throw IllegalArgumentException("duplicate extension")
        }
        loaderByExtension[extension] = container.createInstance(loaderType)
    }

    fun loadAll() {
        val filesByExt = fileService.getFiles("assets")
            .groupBy { file -> file.getExtension() }
        for ((extension, files) in filesByExt) {
            val loader = loaderByExtension[extension] ?: throw IllegalArgumentException()
            val cache = cacheByType[loader.getType()] ?: throw IllegalArgumentException()

            val assets = loader.load(files)
            for ((name, asset) in assets) {
                cache[name] = asset
            }
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
