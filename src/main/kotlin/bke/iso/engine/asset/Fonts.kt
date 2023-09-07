package bke.iso.engine.asset

import bke.iso.engine.Disposer
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import mu.KotlinLogging
import java.io.File
import kotlin.math.ceil

private const val MINIMUM_FONT_SIZE = 5

class Fonts(
    private val assets: Assets,
    private val renderer: Renderer
) {

    private val log = KotlinLogging.logger {}

    private val cache = mutableMapOf<FontOptions, BitmapFont>()

    operator fun get(options: FontOptions): BitmapFont =
        cache.getOrPut(options) { generateFont(options) }

    operator fun contains(font: BitmapFont) =
        cache.containsValue(font)

    private fun generateFont(options: FontOptions): BitmapFont {
        val displayMode = renderer.maxDisplayMode
        val aspectRatio = displayMode.width.toFloat() / displayMode.height.toFloat()
        val scaledSize = options.size * aspectRatio
        val pixels = ceil(scaledSize)
            .toInt()
            .coerceAtLeast(MINIMUM_FONT_SIZE)

        val generator = assets.get<FreeTypeFontGenerator>(options.name)
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = pixels
            color = options.color
            genMipMaps = true
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.MipMapLinearNearest
        }

        val bitmapFont = generator.generateFont(parameter)
        log.debug { "Generated font: '$bitmapFont' aspectRatio: $aspectRatio size: $scaledSize -> $pixels" }
        return generator.generateFont(parameter)
    }

    fun dispose() {
        log.info { "Disposing fonts" }
        for ((options, font) in cache) {
            Disposer.dispose(font, options.name)
        }
    }
}

class FreeTypeFontGeneratorLoader : AssetLoader<FreeTypeFontGenerator> {
    override fun load(file: File): FreeTypeFontGenerator =
        FreeTypeFontGenerator(FileHandle(file))
}

data class FontOptions(
    val name: String,
    val size: Float,
    val color: Color = Color.WHITE
)
