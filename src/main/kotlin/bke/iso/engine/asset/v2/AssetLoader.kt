package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import kotlin.reflect.KClass

interface AssetLoader<T : Any> {
    fun assetType(): KClass<T>

   fun load(file: FilePointer): Pair<String, T>
}
