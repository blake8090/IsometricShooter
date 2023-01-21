package bke.iso.engine.asset

import bke.iso.engine.log
import com.badlogic.gdx.utils.Disposable
import kotlin.reflect.KClass

class AssetCache {
    private val assets = mutableMapOf<Pair<String, KClass<*>>, Asset<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String, kClass: KClass<T>): Asset<T>? =
        assets[name to kClass] as? Asset<T>

    fun <T : Any> add(asset: Asset<T>) {
        val kClass = asset.value::class
        // TODO: notify if duplicate
        assets[asset.name to kClass] = asset
    }

    fun dispose() {
        assets.values.forEach(this::dispose)
        assets.clear()
    }

    private fun dispose(asset: Asset<*>) {
        val value = asset.value
        if (value is Disposable) {
            value.dispose()
            log.info("Disposed asset - name: '${asset.name}', type: '${asset.value::class.simpleName}'")
        }
    }
}
