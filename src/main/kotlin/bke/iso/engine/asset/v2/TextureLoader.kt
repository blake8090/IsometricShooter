package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.service.Transient
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture

@Transient
class TextureLoader : AssetLoader<Texture> {
    override fun assetType() = Texture::class

    override fun load(file: FilePointer): Pair<String, Texture> {
        val name = file.getNameWithoutExtension()
        val handle = FileHandle(file.getRawFile())
        val value = Texture(handle)
        return name to value
    }
}
