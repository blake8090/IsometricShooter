package bke.iso.engine.asset

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File

class TextureCache : AssetCache<Texture>() {
    override val extensions: Set<String> = setOf("png", "jpg")

    override suspend fun loadAssets(file: File): List<LoadedAsset<Texture>> {
        val texture = Texture(FileHandle(file), true)
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        return listOf(LoadedAsset(file.name, texture))
    }
}
