package bke.iso.engine.world.asset

import bke.iso.engine.util.FilePointer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class TileTemplateLoaderTest {
    private val tileTemplateLoader = TileTemplateLoader()

    @Test
    fun `should return tile templates`() {
        val files = mutableListOf(getResource("tile-templates/valid.toml"))

        val tileTemplates = TileTemplateLoader().loadAssets(files)

        assertThat(tileTemplates.values).containsExactlyInAnyOrder(
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
    fun `should skip invalid templates`() {
        val files = mutableListOf(
            getResource("tile-templates/valid.toml"),
            getResource("tile-templates/invalid.toml")
        )

        val tileTemplates = TileTemplateLoader().loadAssets(files)

        assertThat(tileTemplates.values).containsExactlyInAnyOrder(
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
    fun `should skip templates with duplicate names`() {
        val files = mutableListOf(
            getResource("tile-templates/valid.toml"),
            getResource("tile-templates/duplicate.toml")
        )

        val tileTemplates = TileTemplateLoader().loadAssets(files)

        assertThat(tileTemplates.values).containsExactlyInAnyOrder(
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
        assertThat(tileTemplateLoader.getAssetType()).isEqualTo(TileTemplate::class)
    }

    private fun getResource(name: String): FilePointer {
        val path = this.javaClass
            .classLoader
            .getResource(name)
            .path
        return FilePointer(File(path))
    }
}
