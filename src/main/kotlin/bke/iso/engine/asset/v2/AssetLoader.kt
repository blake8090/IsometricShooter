package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer

interface AssetLoader<T : Any> {
    fun load(file: FilePointer): Pair<String, T>
}

inline fun <reified T : Any> AssetLoader<T>.getType() =
    T::class
