package bke.iso.old.engine.asset

import bke.iso.old.engine.FilePointer
import bke.iso.old.service.TransientService

interface AssetLoader<T : Any> : TransientService {
    fun load(file: FilePointer): Pair<String, T>
}
