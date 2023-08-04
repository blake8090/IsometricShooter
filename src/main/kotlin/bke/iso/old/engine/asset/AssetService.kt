package bke.iso.old.engine.asset

import bke.iso.old.engine.FileService
import bke.iso.old.service.ServiceProvider
import bke.iso.old.service.SingletonService
import kotlin.reflect.KClass

class AssetService(
    private val fileService: FileService,
    private val provider: ServiceProvider<AssetLoader<*>>
) : SingletonService {

    private val loadersByExtension = mutableMapOf<String, AssetLoader<*>>()
    private var currentModule: AssetModule? = null

    override fun create() {
        addLoader("jpg", provider.get(TextureLoader::class))
        addLoader("png", provider.get(TextureLoader::class))
    }

    override fun dispose() {
        currentModule?.unload()
    }

    fun addLoader(fileExtension: String, loader: AssetLoader<*>) {
        val existingLoader = loadersByExtension[fileExtension]
        if (existingLoader != null) {
            throw IllegalArgumentException(
                "Extension '$fileExtension' has already been set to loader ${existingLoader::class.simpleName}"
            )
        }
        loadersByExtension[fileExtension] = loader
    }

    fun <T : AssetLoader<*>> addLoader(fileExtension: String, type: KClass<T>) =
        addLoader(fileExtension, provider.get(type))

    inline fun <reified T : AssetLoader<*>> addLoader(extension: String) =
        addLoader(extension, T::class)

    fun loadModule(name: String) {
        currentModule?.unload()
        currentModule = AssetModule(name).apply { load(fileService, loadersByExtension) }
    }

    fun <T : Any> get(name: String, type: KClass<T>): T? {
        if (currentModule == null) {
            throw IllegalStateException("No module was loaded")
        }
        return currentModule!!.get(name, type)
    }

    inline fun <reified T : Any> get(name: String) =
        get(name, T::class)

    fun <T : Any> require(name: String, type: KClass<T>): T =
        get(name, type) ?: throw IllegalStateException(
            "Asset $name (${type.simpleName} was not loaded in module ${currentModule!!.name}"
        )

    inline fun <reified T : Any> require(name: String) =
        require(name, T::class)
}
