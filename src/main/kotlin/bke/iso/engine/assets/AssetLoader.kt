package bke.iso.engine.assets

import bke.iso.engine.FilePointer
import bke.iso.engine.log
import kotlin.reflect.KClass

abstract class AssetLoader<T : Any> {
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
                    log.info("Loaded asset '${asset.name}' as type '${getType().simpleName}' from file '${file.getPath()}'")
                }
            }
        }

        return assets
    }
}
