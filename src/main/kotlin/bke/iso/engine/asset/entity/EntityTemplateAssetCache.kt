package bke.iso.engine.asset.entity

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EntityTemplateAssetCache(private val serializer: Serializer) : AssetCache<EntityTemplate>() {
    override val extensions: Set<String> = setOf("entity")

    override suspend fun load(file: File) =
        withContext(Dispatchers.IO) {
            val template = serializer.read<EntityTemplate>(file.readText())
            store(file, template.name, template)
        }
}
