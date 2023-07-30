package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.service.TransientService
import kotlin.reflect.KClass

abstract class AssetLoader<T : Any> : TransientService {
    abstract val type: KClass<T>

    abstract fun load(file: FilePointer): Pair<String, T>
}
