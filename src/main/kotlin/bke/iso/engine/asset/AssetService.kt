package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import bke.iso.engine.log
import bke.iso.service.ServiceProvider
import bke.iso.service.SingletonService
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.reflect.KClass

private const val ASSETS_DIRECTORY = "assets"

class AssetService(
    private val fileService: FileService,
    private val provider: ServiceProvider<AssetLoader<*>>
) : SingletonService {

    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()

    private val cacheByModule = mutableMapOf<String, AssetCache>()
    private var currentModule: String? = null

    override fun create() {
        addLoader<TextureLoader>("png")
        addLoader<TextureLoader>("jpg")
    }

    fun <T : AssetLoader<*>> addLoader(extension: String, type: KClass<T>) {
        val existingLoader = loaderByExtension[extension]
        if (existingLoader != null) {
            throw Error("Asset loader '${existingLoader::class.simpleName}' already defined for extension '$extension'")
        }
        loaderByExtension[extension] = provider.get(type)
    }

    inline fun <reified T : AssetLoader<*>> addLoader(extension: String) =
        addLoader(extension, T::class)

    fun <T : Any> get(name: String, kClass: KClass<T>): T? =
        getCache()
            .get(name, kClass)
            ?.value

    inline fun <reified T : Any> get(name: String): T? =
        get(name, T::class)

    private fun getCache(): AssetCache {
        if (currentModule == null) {
            throw Error("no module loaded")
        }
        return cacheByModule[currentModule] ?: throw Error("module was not loaded")
    }

    override fun dispose() {
        for ((module, cache) in cacheByModule) {
            cache.dispose()
            log.info("Disposed module '$module'")
        }
        cacheByModule.clear()
    }

    fun loadModule(moduleName: String) {
        val path = Path(ASSETS_DIRECTORY, moduleName)
        if (path.notExists() || !path.isDirectory()) {
            throw Error("Module directory '$path' was not found")
        }

        val filesByExtension = fileService
            .getFiles(path.pathString)
            .groupBy(FilePointer::getExtension)
        if (filesByExtension.isEmpty()) {
            throw Error("No assets found in module '$path'")
        }

        // TODO: unload previously loaded module
        cacheByModule[moduleName] = AssetCache()
        currentModule = moduleName
        filesByExtension.forEach(this::loadFiles)
        log.info("Module '$moduleName' has been successfully loaded")
    }

    private fun loadFiles(extension: String, files: List<FilePointer>) {
        val assetLoader = loaderByExtension[extension]
        if (assetLoader == null) {
            log.warn("No asset loader for file extension '.$extension' was defined, skipping ${files.size} file(s)")
            return
        }

        val cache = cacheByModule[currentModule] ?: throw Error("Expected cache for module '$currentModule'")
        val assets = files.map { file -> loadAsset(file, assetLoader) }
        for (asset in assets) {
            cache.add(asset)
            log.info(
                "Loaded asset - name: '${asset.name},"
                        + " type: '${assetLoader.assetType().simpleName}',"
                        + " path: '${asset.path}'"
            )
        }
    }

    private fun <T : Any> loadAsset(file: FilePointer, assetLoader: AssetLoader<T>): Asset<T> {
        val (name, value) = assetLoader.load(file)
        return Asset(
            name,
            file.getPath(),
            file.getExtension(),
            value
        )
    }
}
