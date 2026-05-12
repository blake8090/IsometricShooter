package bke.iso.engine.asset

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.OrderedMap
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

abstract class AssetCache<T : Any> {

    private val log = KotlinLogging.logger {}

    abstract val extensions: Set<String>

    private val assetsByName = OrderedMap<String, T>()

    fun get(name: String): T? =
        assetsByName[name]

    fun contains(asset: T): Boolean =
        assetsByName.containsValue(asset, false)

    fun getAll(): List<T> =
        assetsByName.values().toList()

    fun getAllByName(): List<Pair<String, T>> =
        assetsByName
            .entries()
            .map { entry -> entry.key to entry.value }

    abstract suspend fun load(file: File)

    protected fun store(file: File, name: String, asset: T) {
        assetsByName.put(name, asset)
        log.info { "Loaded asset '${name}' (${asset::class.simpleName}) from '${file.canonicalPath}'" }
    }

    fun dispose(assetDisposer: AssetDisposer) {
        for (entry in assetsByName) {
            val path = entry.key
            val asset = entry.value
            if (asset is Disposable) {
                assetDisposer.dispose(path, asset)
            }
        }
        assetsByName.clear()
    }
}
