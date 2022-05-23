package bke.iso.asset

class AssetCache<T : Any> {
    private val cache = mutableMapOf<String, T>()

    operator fun set(name: String, asset: T) {
        cache[name] = asset
    }
}
