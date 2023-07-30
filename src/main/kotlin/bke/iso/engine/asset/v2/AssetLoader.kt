package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.service.TransientService

interface AssetLoader<T : Any> : TransientService {
    fun load(file: FilePointer): Pair<String, T>
}
