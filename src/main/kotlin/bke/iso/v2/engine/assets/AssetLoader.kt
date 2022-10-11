package bke.iso.v2.engine.assets

import bke.iso.engine.util.getLogger
import bke.iso.v2.engine.FilePointer
import kotlin.reflect.KClass

data class Asset<T>(
    val name: String,
    val asset: T
)

typealias AssetMap<T> = MutableMap<String, Asset<T>>

abstract class AssetLoader<T : Any> {
    private val log = getLogger()

    abstract fun getType(): KClass<T>

    protected abstract fun load(file: FilePointer): List<Asset<T>>

    fun load(files: List<FilePointer>): Map<String, Asset<T>> {
        val assets = mutableMapOf<String, Asset<T>>()

        files.forEach { file ->
            load(file).forEach { asset ->
                if (assets.containsKey(asset.name)) {
                    log.warn("Duplicate asset '${asset.name}' from file '${file.getPath()}'")
                } else {
                    assets[asset.name] = asset
                }
            }
        }

        return assets
    }
}
