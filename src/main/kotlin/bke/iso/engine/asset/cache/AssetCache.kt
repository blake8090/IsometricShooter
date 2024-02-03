package bke.iso.engine.asset.cache

import bke.iso.engine.asset.AssetDisposer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.OrderedMap
import java.io.File

data class LoadedAsset<T : Any>(
    val name: String,
    val asset: T
)

abstract class AssetCache<T : Any> {

    abstract val extensions: Set<String>
    private val assetToName = OrderedMap<String, T>()

    operator fun get(name: String): T? =
        assetToName.get(name)

    operator fun contains(asset: T): Boolean =
        assetToName.containsValue(asset, false)

    fun getAll(): List<T> =
        assetToName.values().toList()

    suspend fun load(file: File): List<LoadedAsset<T>> {
        val loadedAssets = loadAssets(file)
        for ((name, asset) in loadedAssets) {
            assetToName.put(name, asset)
        }
        return loadedAssets
    }

    protected abstract suspend fun loadAssets(file: File): List<LoadedAsset<T>>

    fun dispose(assetDisposer: AssetDisposer) {
        for (entry in assetToName) {
            val name = entry.key
            val asset = entry.value
            if (asset is Disposable) {
                assetDisposer.dispose(asset, name)
            }
        }
        assetToName.clear()
    }
}
