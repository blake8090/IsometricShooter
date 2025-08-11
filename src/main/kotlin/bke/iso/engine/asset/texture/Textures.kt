package bke.iso.engine.asset.texture

import bke.iso.engine.asset.Assets
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

class Textures(private val assets: Assets) {

    private val log = KotlinLogging.logger { }

    private val pageWidth = 2048
    private val pageHeight = 2048

    private val packer = PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, 2, false)
    private lateinit var atlas: TextureAtlas

    fun generateAtlas() {
        for ((name, texture) in assets.getAllByName<Texture>()) {
            texture.textureData.prepare()
            val pixmap = texture.textureData.consumePixmap()
            packer.pack(name, pixmap)
            texture.textureData.disposePixmap()
            log.debug { "Added texture $name to Pixmap" }
        }

        atlas = packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, true)

//        saveDebugTexture()
    }

    private fun saveDebugTexture() {
        var pageIndex = 0
        for (page in packer.pages) {
            val pageTexture = page.pixmap
            val fileHandle =
                FileHandle(File("${System.getenv("LOCALAPPDATA")}\\IsometricShooter", "atlas_page_$pageIndex.png"))
            PixmapIO.writePNG(fileHandle, pageTexture)
            pageIndex++
        }
    }

    fun findRegion(name: String): TextureRegion =
        checkNotNull(atlas.findRegion(name)) {
            "Region $name not found in global atlas"
        }

    fun dispose() {
        packer.dispose()
        atlas.dispose()
    }
}
