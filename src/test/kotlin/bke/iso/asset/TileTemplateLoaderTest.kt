package bke.iso.asset

import bke.iso.util.FileService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class TileTemplateLoaderTest {
    private val fileService = FileService()
    private val tileTemplateLoader = TileTemplateLoader(fileService)

    @Test
    fun `should return tile templates`() {
        val path = this.javaClass.classLoader.getResource("base.tiles").path
        val file = fileService.getFile(path)

        val tileTemplates = tileTemplateLoader.loadAsset(file)
            ?: fail("Expected tileTemplates to not be null")
        assertThat(tileTemplates.templates).containsExactly(
            TileTemplate(
                name = "floor",
                symbol = '.',
                texture = "floor",
                collidable = false,
            ),
            TileTemplate(
                name = "wall",
                symbol = '#',
                texture = "wall",
                collidable = true
            )
        )
    }

    @Test
    fun `should return type`() {
        assertThat(tileTemplateLoader.getAssetType()).isEqualTo(TileTemplates::class)
    }
}
