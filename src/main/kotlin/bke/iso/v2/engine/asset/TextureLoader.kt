package bke.iso.v2.engine.asset

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File

class TextureLoader : AssetLoader<Texture> {

    override fun load(file: File): Pair<String, Texture> {
        val name = file.nameWithoutExtension
        val value = Texture(FileHandle(file))
        return name to value
    }
}
