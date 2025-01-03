package bke.iso.engine.asset.font

import bke.iso.engine.os.SystemInfo
import bke.iso.engine.asset.AssetDisposer
import bke.iso.engine.asset.Assets
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.ceil

data class FontOptions(
    val name: String,
    val size: Float,
    val color: Color = Color.WHITE
)

/**
 * see [asd](https://github.com/libgdx/libgdx/issues/6820)
 */
private const val MINIMUM_FONT_SIZE = 5
private const val REFERENCE_WIDTH = 2560f

class Fonts(
    private val assets: Assets,
    private val systemInfo: SystemInfo
) {

    private val log = KotlinLogging.logger {}

    private val cache = mutableMapOf<FontOptions, BitmapFont>()

    // TODO: use multiple params for cleanliness
    operator fun get(options: FontOptions): BitmapFont =
        cache.getOrPut(options) { generateFont(options) }

    operator fun contains(asset: Any) =
        asset is BitmapFont && cache.containsValue(asset)

    private fun generateFont(options: FontOptions): BitmapFont {
        // TODO: cache scale, simplify formula and add comments
        val baseScale = 0.5f
        val density = systemInfo.screenDensity
        val widthRatio = systemInfo.maxDisplayMode.width / REFERENCE_WIDTH

        val scale = (baseScale + density) * widthRatio
        log.debug {
            "Calculating size - baseScale: $baseScale density: $density, widthRatio: $widthRatio, scale: $scale"
        }

        val scaledSize = options.size * scale
        val pixels = ceil(scaledSize)
            .toInt()
            .coerceAtLeast(MINIMUM_FONT_SIZE)

        val generator = assets.get<FreeTypeFontGenerator>(options.name)
        generator.scaleForPixelHeight(pixels)
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = pixels
            color = options.color
            genMipMaps = true
            minFilter = Texture.TextureFilter.Nearest
            magFilter = Texture.TextureFilter.MipMapLinearNearest
        }

        val bitmapFont = generator.generateFont(parameter)
        log.debug { "Generated font: '$bitmapFont', dp: ${options.size}, scaledSize: $scaledSize -> $pixels" }
        return generator.generateFont(parameter)
    }

    fun dispose(assetDisposer: AssetDisposer) {
        log.debug { "Disposing fonts" }
        for (font in cache.values) {
            assetDisposer.dispose(font.data.name, font)
        }
        cache.clear()
    }
}
