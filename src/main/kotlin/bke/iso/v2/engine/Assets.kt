package bke.iso.v2.engine

import bke.iso.engine.asset.ASSETS_DIRECTORY
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import java.io.File
import kotlin.io.path.Path

class Assets(game: Game) : Module(game) {
    private val textures = mutableMapOf<String, Texture>()

    fun loadTexture(path: String) {
        val fullPath = Path(ASSETS_DIRECTORY, path).toString()
        val file = File(fullPath)
        if (!file.exists()) {
            throw IllegalArgumentException("file $fullPath not found: ${file.canonicalPath}")
        }
        textures[file.nameWithoutExtension] = Texture(FileHandle(file))
    }

    fun getTexture(name: String) =
        textures[name]
}
