package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import bke.iso.service.v2.TransientService
import kotlin.reflect.KClass

interface AssetLoader<T : Any> : TransientService {
    fun assetType(): KClass<T>

    fun load(file: FilePointer): Pair<String, T>
}
