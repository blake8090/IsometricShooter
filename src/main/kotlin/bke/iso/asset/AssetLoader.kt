package bke.iso.asset

import bke.iso.util.FilePointer
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AssetLoader(val fileExtensions: Array<String>)

abstract class BaseAssetLoader<T : Any> {
    abstract fun loadAsset(file: FilePointer): T?
    abstract fun getAssetType(): KClass<T>
}
