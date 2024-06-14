package bke.iso.engine.scene

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SceneCache(private val serializer: Serializer) : AssetCache<Scene>() {

    override val extensions: Set<String> = setOf("scene")

    override suspend fun load(file: File) {
        withContext(Dispatchers.IO) {
            val scene = serializer.read<Scene>(file.readText())
            store(file, file.name, scene)
        }
    }
}
