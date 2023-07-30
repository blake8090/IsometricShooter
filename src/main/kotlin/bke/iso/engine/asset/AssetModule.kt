package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import bke.iso.engine.log
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Asset<T>(
    val name: String,
    val value: T
)

private data class Key<T : Any>(
    val name: String,
    val type: KClass<T>
)

const val ASSETS_DIRECTORY = "assets"

class AssetModule(val name: String) {

    private val assets = mutableMapOf<Key<*>, Asset<*>>()

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        val key = Key(name, type)
        val value = assets[key]
            ?.value
            ?: return null
        return type.safeCast(value)
    }

    inline fun <reified T : Any> get(name: String): T? =
        get(name, T::class)

    fun load(fileService: FileService, loadersByExtension: Map<String, AssetLoader<*>>) {
        val path = Path(ASSETS_DIRECTORY, name)
        val files = fileService.getFiles(path.pathString)
        check(files.isNotEmpty()) {
            "no files found"
        }

        for (file in files) {
            val extension = file.getExtension()
            val assetLoader = loadersByExtension[extension]
            if (assetLoader == null) {
                log.warn("No asset loader found for extension '$extension' - skipping file ${file.getPath()}")
                continue
            }
            loadAsset(file, assetLoader)
        }
    }

    private fun <T : Any> loadAsset(file: FilePointer, assetLoader: AssetLoader<T>) {
        val asset = assetLoader
            .load(file)
            .let { (name, asset) -> Asset(name, asset) }

        val type = asset.value::class
        val key = Key(asset.name, type)
        check(!assets.contains(key)) {
            "duplicate asset"
        }
        assets[key] = asset
        log.info("Loaded asset '${file.getPath()}' (${type.simpleName}) as '${asset.name}' ")
    }

    fun unload() {
        assets.clear()
    }
}
