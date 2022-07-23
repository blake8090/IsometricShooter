package bke.iso.engine.asset

import bke.iso.engine.util.FilePointer
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AssetLoader(val path: String, val fileExtensions: Array<String>)

abstract class BaseAssetLoader<T : Any> {
    abstract fun loadAssets(files: List<FilePointer>): Map<String, T>
    abstract fun getAssetType(): KClass<T>
}
