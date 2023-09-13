package bke.iso.engine.asset.loader

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File

class TextureLoader : AssetLoader<Texture> {

    override val extensions: List<String> = listOf("jpg", "png")

    override suspend fun load(file: File): Texture {
        val texture = Texture(FileHandle(file), true)
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        return texture
    }
}
