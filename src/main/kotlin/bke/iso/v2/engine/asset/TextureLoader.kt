package bke.iso.v2.engine.asset

import bke.iso.engine.FilePointer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture

class TextureLoader : AssetLoader<Texture> {

    override fun load(file: FilePointer): Pair<String, Texture> {
        val name = file.getNameWithoutExtension()
        val handle = FileHandle(file.getRawFile())
        val value = Texture(handle)
        return name to value
    }
}
