package bke.iso.engine.asset

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File

class TextureLoader : AssetLoader<Texture> {

    override fun load(file: File): Pair<String, Texture> {
        val name = file.nameWithoutExtension
        val texture = Texture(FileHandle(file), true)
        texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest)
        return name to texture
    }
}
