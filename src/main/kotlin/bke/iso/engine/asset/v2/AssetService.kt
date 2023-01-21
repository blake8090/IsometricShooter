package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import bke.iso.engine.log
import bke.iso.service.PostInit
import bke.iso.service.Singleton
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.pathString
import kotlin.reflect.KClass

private const val ASSETS_DIRECTORY = "assets"

@Singleton
class AssetService2(private val fileService: FileService) {

    private val loaderByExtension = mutableMapOf<String, AssetLoader<*>>()

    private val cacheByModule = mutableMapOf<String, AssetCache>()
    private var currentModule: String? = null

    @PostInit
    fun setup() {
//        loadModule("test")
    }

    fun <T : Any> get(name: String, kClass: KClass<T>): Asset<T>? =
        getCache().get(name, kClass)

    private fun getCache(): AssetCache {
        if (currentModule == null) {
            throw Error("no module loaded")
        }
        return cacheByModule[currentModule] ?: throw Error("module was not loaded")
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
        val assets = files.map { file -> getAsset(file, assetLoader) }
        for (asset in assets) {
            cache.add(asset)
            log.info(
                "Loaded asset '${asset.name}"
                        + " as type '${assetLoader.assetType().simpleName}'"
                        + " from file '${asset.canonicalPath}'"
            )
        }
    }

    private fun <T : Any> getAsset(file: FilePointer, assetLoader: AssetLoader<T>): Asset<T> {
        val (name, value) = assetLoader.load(file)
        return Asset(
            name,
            file.getPath(),
            file.getExtension(),
            value
        )
    }
}
