package bke.iso.asset

import bke.iso.*
import bke.iso.util.FileService
import bke.iso.util.ReflectionService
import bke.iso.util.getLogger
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

// TODO: add unit tests for failure scenarios
@Service
class AssetService(
    private val container: IocContainer,
    private val fileService: FileService,
    private val reflectionService: ReflectionService
) {
    private val log = getLogger(this)

    private val assetLoaderByPath = mutableMapOf<String, BaseAssetLoader<*>>()
    private val assetCacheByType = mutableMapOf<KClass<*>, MutableMap<String, Any>>()

    @Suppress("UNCHECKED_CAST")
    fun setupAssetLoaders(basePackage: String) {
        val types =
            reflectionService.findSubTypesWithAnnotation(basePackage, AssetLoader::class, BaseAssetLoader::class)
        setupAssetLoaders(types as List<KClass<BaseAssetLoader<Any>>>)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> setupAssetLoaders(types: List<KClass<out BaseAssetLoader<out T>>>) =
        types.forEach { type -> setupAssetLoader(type as KClass<BaseAssetLoader<Any>>) }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getAsset(name: String, type: KClass<T>): T? {
        val cache = assetCacheByType[type]
            ?: throw IllegalArgumentException(
                "Attempted to retrieve asset '$name' with unknown asset type '${type.simpleName}'"
            )
        return cache[name] as T?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getAllAssets(type: KClass<T>): List<T> {
        val cache = assetCacheByType[type]
            ?: throw IllegalArgumentException(
                "Attempted to retrieve all assets with unknown asset type '${type.simpleName}'"
            )
        return cache.values.toList() as List<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun loadAssets(basePath: String) {
        if (basePath.isEmpty()) {
            log.warn("Base path cannot be empty")
            return
        }

        log.info("Loading assets in base path '$basePath'")
        val loadingStartTime = System.currentTimeMillis()
        for ((path, assetLoader) in assetLoaderByPath) {
            loadAssetsInPath("$basePath/${path}", assetLoader as BaseAssetLoader<Any>)
        }
        val loadingEndTime = System.currentTimeMillis()
        log.info("Finished loading assets in base path '$basePath' in ${loadingEndTime - loadingStartTime} millis")
    }

    private inline fun <reified T : Any> setupAssetLoader(type: KClass<BaseAssetLoader<T>>) {
        val annotation = type.findAnnotation<AssetLoader>()
            ?: throw IllegalArgumentException("Expected AssetLoader annotation on type ${type.simpleName}")

        val path = annotation.path
        if (path.isBlank()) {
            throw IllegalArgumentException("Asset loader ${type.simpleName} is missing a path")
        } else if (assetLoaderByPath.containsKey(path)) {
            throw IllegalArgumentException("Asset loader ${type.simpleName} is missing a path")
        } else if (annotation.fileExtensions.isEmpty()) {
            throw IllegalArgumentException("Asset loader ${type.simpleName} is missing file extensions")
        }

        val assetLoader = container.createInstance(type)
        assetLoaderByPath[path] = assetLoader
        assetCacheByType.putIfAbsent(assetLoader.getAssetType(), mutableMapOf())

        log.info(
            "Setup asset loader '{}' for path '{}' and type '{}'",
            type.simpleName,
            path,
            assetLoader.getAssetType().simpleName
        )
    }

    private inline fun <reified T : Any> loadAssetsInPath(path: String, assetLoader: BaseAssetLoader<T>) {
        val typeName = assetLoader.getAssetType().simpleName
        log.info("Loading '$typeName' assets in path '$path'")

        val annotation = assetLoader::class.findAnnotation<AssetLoader>()
            ?: throw IllegalArgumentException("Expected AssetLoader annotation on type ${assetLoader::class.simpleName}")
        val files = fileService.getFiles(path)
            .filter { file -> annotation.fileExtensions.contains(file.getExtension()) }
        // TODO: what happens if files is empty?

        val assetsByName = assetLoader.loadAssets(files)
        val assetCache = assetCacheByType[assetLoader.getAssetType()]
            ?: throw IllegalArgumentException("Expected cache for asset type $typeName")
        for ((name, asset) in assetsByName) {
            if (!assetCache.containsKey(name)) {
                assetCache[name] = asset
                log.info("Asset loaded - type: '$typeName' name: '$name'")
            } else {
                // TODO: finish writing warn message
                log.warn("Duplicate asset ...")
            }
        }
    }
}
