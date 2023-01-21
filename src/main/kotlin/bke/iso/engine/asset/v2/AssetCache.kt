package bke.iso.engine.asset.v2

import kotlin.reflect.KClass

class AssetCache {
    private val assets = mutableMapOf<Pair<String, KClass<*>>, Asset<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String, kClass: KClass<T>): Asset<T>? =
        assets[name to kClass] as? Asset<T>

    inline fun <reified T : Any> get(name: String): Asset<T>? =
        get(name, T::class)

    fun <T : Any> set(kClass: KClass<T>, asset: Asset<T>) {
        // TODO: notify if duplicate
        assets[asset.name to kClass] = asset
    }
}
