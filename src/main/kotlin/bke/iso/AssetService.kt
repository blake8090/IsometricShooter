package bke.iso

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import org.slf4j.LoggerFactory
import java.io.File

@Service
class AssetService {
    private val log = LoggerFactory.getLogger(AssetService::class.java)

    private val texturesByName = mutableMapOf<String, Texture>()

    fun loadAllAssets() {
        KtxAsync.launch {
            File("assets")
                .walkTopDown()
                .filter(File::isFile)
                .forEach { loadAsset(it) }
        }
    }

    private fun loadAsset(file: File) {
        when (file.extension) {
            "png", "jpg", "jpeg" -> loadTexture(file)
            else -> log.warn("Unrecognized file '$file'")
        }
    }

    fun getTexture(name: String): Texture? {
        return texturesByName[name]
    }

    private fun loadTexture(file: File) {
        val textureName = file.nameWithoutExtension
        if (texturesByName.containsKey(textureName)) {
            log.warn("Duplicate texture '$textureName'")
        } else {
            log.info("Loading texture '${file.path}'")
            texturesByName[textureName] = Texture(FileHandle(file))
        }
        log.info("Finished loading texture '${file.path}' as Texture asset '$textureName'")
    }
}
