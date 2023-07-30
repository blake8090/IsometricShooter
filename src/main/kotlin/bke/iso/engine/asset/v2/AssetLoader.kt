package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.service.TransientService
import kotlin.reflect.KClass

interface AssetLoader<T : Any> : TransientService {
    fun load(file: FilePointer): Pair<String, T>
}

inline fun <reified T : Any> AssetLoader<T>.type(): KClass<T> = T::class
