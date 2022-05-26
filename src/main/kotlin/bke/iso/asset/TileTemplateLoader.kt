package bke.iso.asset

import bke.iso.util.FilePointer
import bke.iso.util.getLogger
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import java.io.File
import java.util.Collections
import kotlin.reflect.KClass

data class TileTemplate(
    val name: String = "",
    val symbol: Char = '.',
    val texture: String = "",
    val collidable: Boolean = false
)

@AssetLoader("tiles", ["dat"])
class TileTemplateLoader : BaseAssetLoader<TileTemplate>() {
    private val log = getLogger(this)
    private val mapper = TomlMapper()

    override fun loadAssets(files: List<FilePointer>): Map<String, TileTemplate> {
        val assets = mutableMapOf<String, TileTemplate>()

        for (file in files.map(FilePointer::getRawFile)) {
            val fields = readFields(file)

            for ((name, node) in fields) {
                if (assets.containsKey(name)) {
                    log.warn("Duplicate template '$name' found in file '${file.path}', skipping")
                    continue
                }

                try {
                    val tileTemplate = mapper.treeToValue(node, TileTemplate::class.java)
                    // ensures that the TOML node name corresponds to the template name
                    assets[name] = tileTemplate.copy(name = name)
                } catch (e: JsonProcessingException) {
                    log.error("Could not load tile template '$name' from file '${file.path}'", e)
                }
            }
        }

        return assets
    }

    override fun getAssetType(): KClass<TileTemplate> =
        TileTemplate::class

    private fun readFields(file: File): MutableIterator<MutableMap.MutableEntry<String, JsonNode>> =
        try {
            mapper.readTree(file).fields()
        } catch (ex: JsonProcessingException) {
            log.error("Could not load tile templates from file '${file.path}'", ex)
            Collections.emptyIterator()
        }
}
