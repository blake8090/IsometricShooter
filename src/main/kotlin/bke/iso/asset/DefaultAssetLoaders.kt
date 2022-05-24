package bke.iso.asset

import bke.iso.util.FilePointer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import kotlin.reflect.KClass

@AssetLoader(["png", "jpg"])
class TextureLoader : BaseAssetLoader<Texture>() {
    override fun loadAsset(file: FilePointer): Texture {
        val fileHandle = FileHandle(file.getRawFile())
        return Texture(fileHandle)
    }

    override fun getAssetType(): KClass<Texture> =
        Texture::class
}
