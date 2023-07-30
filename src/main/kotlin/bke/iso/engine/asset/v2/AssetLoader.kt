package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.service.TransientService
import kotlin.reflect.KClass

abstract class AssetLoader<T : Any> : TransientService {
    abstract fun load(file: FilePointer): Pair<String, T>

    inline fun <reified T : Any> getType(): KClass<T> =
        T::class
}
