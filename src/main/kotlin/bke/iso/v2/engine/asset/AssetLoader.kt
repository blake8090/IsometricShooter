package bke.iso.v2.engine.asset

import bke.iso.engine.FilePointer

interface AssetLoader<T : Any> {
    fun load(file: FilePointer): Pair<String, T>
}
