package bke.iso.engine.asset

import bke.iso.engine.util.FilePointer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import kotlin.reflect.KClass

@AssetLoader("gfx", ["png", "jpg"])
class TextureLoader : BaseAssetLoader<Texture>() {
    override fun loadAssets(files: List<FilePointer>): Map<String, Texture> {
        val textures = mutableMapOf<String, Texture>()

        for (file in files) {
            val name = file.getNameWithoutExtension()
            val fileHandle = FileHandle(file.getRawFile())
            // TODO: handle collision
            textures[name] = Texture(fileHandle)
        }

        return textures
    }

    override fun getAssetType(): KClass<Texture> = Texture::class
}
