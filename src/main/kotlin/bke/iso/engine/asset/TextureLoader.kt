package bke.iso.engine.asset

import bke.iso.service.Transient
import bke.iso.engine.FilePointer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import kotlin.reflect.KClass

@Transient
class TextureLoader : AssetLoader<Texture>() {
    override fun getType(): KClass<Texture> =
        Texture::class

    override fun load(file: FilePointer): List<Asset<Texture>> {
        return listOf(
            Asset(
                file.getNameWithoutExtension(),
                Texture(FileHandle(file.getRawFile()))
            )
        )
    }
}
