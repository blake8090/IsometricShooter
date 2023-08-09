package bke.iso.engine.asset

import bke.iso.old.engine.log
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import java.io.File

class Fonts(private val assets: Assets) {

    private val cache = mutableMapOf<FontOptions, BitmapFont>()

    operator fun get(options: FontOptions) =
        cache.getOrPut(options) { generateFont(options) }

    private fun generateFont(options: FontOptions): BitmapFont {
        val generator = assets.get<FreeTypeFontGenerator>(options.name)
        val pixels = (options.dp * Gdx.graphics.density).toInt()
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = pixels
        parameter.color = options.color
        log.debug("Generated font ${options.name}, $options, pixels $pixels")
        return generator.generateFont(parameter)
    }

    fun reload() {
        log.debug("Reloading fonts")
        for (options in cache.keys) {
            cache[options] = generateFont(options)
        }
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
