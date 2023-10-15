package bke.iso.engine.scene

import bke.iso.engine.asset.cache.AssetCache
import bke.iso.engine.asset.cache.LoadedAsset
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SceneCache(private val serializer: Serializer) : AssetCache<Scene>() {

    override val extensions: Set<String> = setOf("scene")

    override suspend fun loadAssets(file: File): List<LoadedAsset<Scene>> =
        withContext(Dispatchers.IO) {
            val scene = serializer.read<Scene>(file.readText())
            listOf(LoadedAsset(file.name, scene))
        }
}
