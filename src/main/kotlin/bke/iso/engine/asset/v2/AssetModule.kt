package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
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

private const val ASSETS_DIRECTORY = "assets"

class AssetModule(private val name: String) {

    private val assets = mutableMapOf<Key<*>, Asset<*>>()

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        val key = Key(name, type)
        val value = assets[key]
            ?.value
            ?: return null
        return type.safeCast(value)
    }

    inline fun <reified T :Any> get(name: String): T? =
        get(name, T::class)

    fun load(fileService: FileService, loadersByExtension: Map<String, AssetLoader<Any>>) {
        val path = Path(ASSETS_DIRECTORY, name)
        val files = fileService.getFiles(path.pathString)
        check(files.isNotEmpty()) {
            "no files found"
        }

        for (file in files) {
            val assetLoader = loadersByExtension[file.getExtension()]
                ?: throw IllegalArgumentException()
            loadAsset(file, assetLoader)
        }
    }

    private fun loadAsset(file: FilePointer, assetLoader: AssetLoader<Any>) {
        val asset = assetLoader
            .load(file)
            .let { (name, asset) -> Asset(name, asset) }

        val key = Key(asset.name, assetLoader.type())
        check(!assets.containsKey(key)) {
            "duplicate"
        }

        assets[key] = asset
    }

    fun unload() {
        assets.clear()
    }
}
