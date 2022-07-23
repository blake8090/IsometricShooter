package bke.iso.engine.world.asset

import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.asset.BaseAssetLoader
import bke.iso.engine.util.FilePointer
import bke.iso.engine.util.getLogger
import kotlin.reflect.KClass

data class MapData(
    val rows: List<List<Char>> = mutableListOf()
)

@AssetLoader("maps", ["map"])
class MapLoader : BaseAssetLoader<MapData>() {
    private val log = getLogger()

    override fun loadAssets(files: List<FilePointer>): Map<String, MapData> {
        val assets = mutableMapOf<String, MapData>()
        for (file in files) {
            val name = file.getNameWithoutExtension()
            if (assets.containsKey(name)) {
                log.warn("Duplicate map '$name' found in file '${file.getPath()}', skipping")
                continue
            }
            val map = readMap(file)
            assets[name] = map
            log.debug("Loaded map '${file.getPath()}' containing ${map.rows.size} rows")
        }
        return assets
    }

    override fun getAssetType(): KClass<MapData> =
        MapData::class

    private fun readMap(file: FilePointer): MapData {
        val rows = file.readText()
            .lines()
            .map(String::toList)
            .filter { chars -> chars.isNotEmpty() }
        return MapData(rows)
    }
}
