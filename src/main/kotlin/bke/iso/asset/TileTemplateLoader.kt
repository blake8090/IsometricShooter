package bke.iso.asset

import bke.iso.util.FilePointer
import bke.iso.util.FileService
import kotlin.reflect.KClass

data class TileTemplate(
    val name: String = "",
    val symbol: Char = '.',
    val texture: String = "",
    val collidable: Boolean = false
)

data class TileTemplates(
    val templates: List<TileTemplate> = mutableListOf()
)

@AssetLoader(["tiles"])
class TileTemplateLoader(private val fileService: FileService) : BaseAssetLoader<TileTemplates>() {
    override fun loadAsset(file: FilePointer): TileTemplates? {
        return fileService.readDataFile(file)
    }

    override fun getAssetType(): KClass<TileTemplates> =
        TileTemplates::class
}
