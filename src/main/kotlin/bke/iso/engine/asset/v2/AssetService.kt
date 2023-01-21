package bke.iso.engine.asset.v2

import bke.iso.engine.FileService
import kotlin.reflect.KClass

const val ASSETS_DIRECTORY = "assets"

class AssetService(private val fileService: FileService) {

    private var packages = mutableSetOf<String>()
    private val modules = mutableMapOf<String, AssetCache>()
    private var currentModule: String? = null

    fun <T : Any> get(name: String, kClass: KClass<T>): Asset<T>? =
        getCache().get(name, kClass)

    private fun getCache(): AssetCache {
        if (currentModule == null) {
            throw Error("no module loaded")
        }
        return modules[currentModule] ?: throw Error("module was not loaded")
    }
}
