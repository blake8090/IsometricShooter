package bke.iso.engine.world.asset

import bke.iso.engine.util.FilePointer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class MapLoaderTest {
    @Test
    fun `should load tiles in expected order`() {
        val files = mutableListOf(getResource("maps/example.map"))

        val assets = MapLoader().loadAssets(files)
        assertThat(assets).hasSize(1)

        val expected = MapData(
            listOf(
                listOf('.', 'a', '.', 'b'),
                listOf('d', '.', '.', 'c')
            )
        )
        assertThat(assets["example"]).isEqualTo(expected)
    }

    private fun getResource(name: String): FilePointer {
        val path = this.javaClass
            .classLoader
            .getResource(name)
            .path
        return FilePointer(File(path))
    }
}