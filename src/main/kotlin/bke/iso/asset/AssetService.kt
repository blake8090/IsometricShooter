package bke.iso.asset

import bke.iso.*
import bke.iso.util.Globals
import bke.iso.util.getLogger
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

// TODO: add unit tests for failure scenarios
@Service
class AssetService(
    private val globals: Globals,
    private val container: IocContainer
) {
    private val log = getLogger(this)

    private val fileExtensionToLoader = mutableMapOf<String, BaseAssetLoader<*>>()
    private val typeToCache = mutableMapOf<KClass<*>, MutableMap<String, Any>>()

    init {
        init()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getAsset(name: String, type: KClass<T>): T? {
        val cache = typeToCache[type]
            ?.let { it as MutableMap<String, T> }
            ?: throw IllegalArgumentException("Expected map for type ${type.simpleName}")
        return cache[name]
    }

    @Suppress("UNCHECKED_CAST")
    fun loadAsset(file: FilePointer) {
        if (file.isDirectory()) {
            log.debug("Skipping directory '{}'", file.getPath())
            return
        }
        val assetLoader = fileExtensionToLoader[file.getExtension()]
        if (assetLoader == null) {
            log.warn(
                "Error when loading file '{}': No asset loader found for extension '{}'",
                file.getPath(),
                file.getExtension()
            )
            return
        }
        loadAsset(file, assetLoader as BaseAssetLoader<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun init() {
        val assetLoaderTypes =
            Reflections(globals.basePackage, Scanners.TypesAnnotated)
                .getTypesAnnotatedWith(AssetLoader::class.java)
                .map { type -> type.kotlin }
                .filter { type -> type.isSubclassOf(BaseAssetLoader::class) }
        log.info("The following types will be loaded: {}", assetLoaderTypes.toString())
        for (type in assetLoaderTypes) {
            setupAssetLoader(type as KClass<BaseAssetLoader<Any>>)
        }
        log.info("init complete")
    }

    private inline fun <reified T : Any> loadAsset(file: FilePointer, assetLoader: BaseAssetLoader<T>) {
        val asset = assetLoader.loadAsset(file)
        if (asset == null) {
            log.warn("Could not load asset {} from file '{}'", assetLoader.getAssetType().simpleName, file.getPath())
            return
        }

        val assetType = assetLoader.getAssetType()
        val assetCache = typeToCache[assetType]
            ?: throw IllegalArgumentException("Expected a map for type ${assetType.simpleName}")
        // TODO: warn/throw if collision
        val name = file.getNameWithoutExtension()
        assetCache[name] = asset

        log.info(
            "Loaded asset '{}' as {} from directory '{}'",
            name,
            assetType.simpleName,
            file.getPath()
        )
    }

    private inline fun <reified T : Any> setupAssetLoader(type: KClass<BaseAssetLoader<T>>) {
        val assetLoader = container.createInstance(type)

        val annotation = type.findAnnotation<AssetLoader>()
            ?: throw IllegalArgumentException("Expected AssetLoader annotation on type ${type.simpleName}")
        // TODO: Throw exception if no extensions provided
        for (fileExtension in annotation.fileExtensions) {
            fileExtensionToLoader[fileExtension] = assetLoader
        }

        val assetType = assetLoader.getAssetType()
        typeToCache.putIfAbsent(assetType, mutableMapOf())
        log.info("Set up asset loader '{}' for type '{}'", type.simpleName, assetType.simpleName)
    }
}
