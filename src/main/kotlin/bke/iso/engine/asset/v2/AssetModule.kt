package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Asset<T>(
    val name: String,
    val path: String,
    val value: T
)

private data class Key<T : Any>(
    val name: String,
    val type: KClass<T>
)

private const val ASSETS_DIRECTORY = "assets"

class AssetModule(val name: String) {

    private val assets = mutableMapOf<Key<*>, Asset<*>>()

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        val key = Key(name, type)
        val value = assets[key]
            ?.value
            ?: return null
        return type.safeCast(value)
    }

    fun load(fileService: FileService, loaderByExtension: Map<String, AssetLoader<*>>) {
        val path = Path(ASSETS_DIRECTORY, name)
        val files = fileService.getFiles(path.pathString)
        require(files.isNotEmpty()) {
            "no files found"
        }

        for (file in files) {
            val assetLoader = loaderByExtension[file.getExtension()]
                ?: throw IllegalArgumentException()
            loadAsset(file, assetLoader)
        }
    }

    private fun <T : Any> loadAsset(file: FilePointer, assetLoader: AssetLoader<T>) {
        val (name, value) = assetLoader.load(file)
        val asset = Asset(name, file.getPath(), value)
        val key = Key(asset.name, assetLoader.type)
        require(!assets.containsKey(key)) {
            "duplicate"
        }
        assets.computeIfPresent(key) { _, _ ->
            throw IllegalArgumentException()
        }
        assets[key] = asset
    }

    fun unload() {
        assets.clear()
    }
}
