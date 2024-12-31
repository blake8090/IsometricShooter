package bke.iso.engine.asset

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File

class TextureAssetCache : AssetCache<Texture>() {
    override val extensions: Set<String> = setOf("png", "jpg")

    override suspend fun load(file: File) {
        val texture = Texture(FileHandle(file), true)
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        store(file, file.name, texture)
    }
}
