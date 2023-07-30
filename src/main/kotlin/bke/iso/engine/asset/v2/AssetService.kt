package bke.iso.engine.asset.v2

import bke.iso.engine.FileService
import bke.iso.service.SingletonService
import kotlin.reflect.KClass

class AssetService(private val fileService: FileService) : SingletonService {

    private val loadersByExtension = mutableMapOf<String, AssetLoader<Any>>()
    private var currentModule: AssetModule? = null

    override fun dispose() {
        currentModule?.unload()
    }

//    fun addLoader(fileExtension: String, loader: AssetLoader<Any>) {
//        // todo: fix incorrect message
//        loadersByExtension[fileExtension]?.let {
//            throw IllegalArgumentException(
//                "Extension '$fileExtension' was already set to '${loader::class.simpleName}<${loader.getType()}>'"
//            )
//        }
//        loadersByExtension[fileExtension] = loader
//    }
//
//    fun loadModule(name: String) {
//        currentModule?.unload()
//        currentModule = AssetModule(name).apply { load(fileService, loadersByExtension) }
//    }
//
//    fun <T : Any> get(name: String, type: KClass<T>): T? {
//        if (currentModule == null) {
//            throw IllegalStateException("No module was loaded")
//        }
//        return currentModule!!.get(name, type)
//    }
//
//    inline fun <reified T : Any> get(name: String) =
//        get(name, T::class)
}
