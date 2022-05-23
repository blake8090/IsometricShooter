package bke.iso.asset

import bke.iso.FilePointer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import kotlin.reflect.KClass

@AssetLoader(["png", "jpg"])
class TextureLoader : BaseAssetLoader<Texture>() {
    override fun loadAsset(file: FilePointer): Texture = Texture(FileHandle(file.getFile()))

    override fun getAssetType(): KClass<Texture> =
        Texture::class
}
