package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import kotlin.reflect.KClass

interface AssetLoader<T : Any> {
    fun assetType(): KClass<T>

    fun load(file: FilePointer): Pair<String, T>
}
