package bke.iso.engine.asset.entity

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.entity.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class EntityTemplate(
    val name: String,
    val components: MutableList<Component>
)

class EntityTemplateAssetCache(private val serializer: Serializer) : AssetCache<EntityTemplate>() {
    override val extensions: Set<String> = setOf("entity")

    override suspend fun load(file: File) =
        withContext(Dispatchers.IO) {
            val template = serializer.read<EntityTemplate>(file.readText())
            store(file, template.name, template)
        }
}
