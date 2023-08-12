package bke.iso.engine.asset

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import mu.KotlinLogging
import java.io.File
import kotlin.math.ceil

class Fonts(private val assets: Assets) {

    private val log = KotlinLogging.logger {}

    private val cache = mutableMapOf<FontOptions, BitmapFont>()

    operator fun get(options: FontOptions) =
        cache.getOrPut(options) { generateFont(options) }

    private fun generateFont(options: FontOptions): BitmapFont {
        val generator = assets.get<FreeTypeFontGenerator>(options.name)
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
//        val pixels = (options.dp * Gdx.graphics.density).toInt()
//        parameter.size = pixels
        val pixels = (options.dp * Gdx.graphics.density)
        parameter.size = ceil(pixels).toInt()
        generator.scaleForPixelHeight(ceil(pixels).toInt())
        parameter.minFilter = Texture.TextureFilter.Nearest
        parameter.magFilter = Texture.TextureFilter.MipMapLinearNearest
        parameter.color = options.color
        log.debug { "Generated font ${options.name}, $options, pixels $pixels" }
        return generator.generateFont(parameter)
    }
}

class FreeTypeFontGeneratorLoader : AssetLoader<FreeTypeFontGenerator> {
    override fun load(file: File): Pair<String, FreeTypeFontGenerator> {
        val generator = FreeTypeFontGenerator(FileHandle(file))
        return file.nameWithoutExtension to generator
    }
}

data class FontOptions(
    val name: String,
    val dp: Float,
    val color: Color = Color.WHITE
)
