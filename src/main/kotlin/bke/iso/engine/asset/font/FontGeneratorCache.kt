package bke.iso.engine.asset.font

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.asset.LoadedAsset
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FontGeneratorCache : AssetCache<FreeTypeFontGenerator>() {
    override val extensions: Set<String> = setOf("ttf")

    override suspend fun loadAssets(file: File): List<LoadedAsset<FreeTypeFontGenerator>> =
        withContext(Dispatchers.IO) {
            val generator = FreeTypeFontGenerator(FileHandle(file))
            listOf(LoadedAsset(file.name, generator))
        }
}
