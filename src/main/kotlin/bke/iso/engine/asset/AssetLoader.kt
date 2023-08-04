package bke.iso.engine.asset

import java.io.File

interface AssetLoader<T : Any> {
    fun load(file: File): Pair<String, T>
}
