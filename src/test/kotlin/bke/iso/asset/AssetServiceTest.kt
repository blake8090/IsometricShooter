package bke.iso.asset

import bke.iso.util.FilePointer
import bke.iso.util.Globals
import bke.iso.IocContainer
import bke.iso.asset.test.Document
import bke.iso.asset.test.DocumentLoader
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class AssetServiceTest {
    private val globals = mock(Globals::class.java)
        .apply {
            `when`(this.basePackage).thenReturn("bke.iso.asset.test")
        }

    @Test
    fun `should load asset using annotated asset loaders`() {
        val container = mock(IocContainer::class.java)
        `when`(container.createInstance(DocumentLoader::class)).thenReturn(DocumentLoader())

        val file = mockFile("data/documents", "txt", "doc1")

        val assetService = AssetService(globals, container)
        assetService.loadAsset(file)
        val document = assetService.getAsset("doc1", Document::class)
            ?: fail("Expected a document")
        assertThat(document).isEqualTo(Document("doc1", "some text"))
    }

    private fun mockFile(path: String, extension: String, name: String): FilePointer {
        return mock(FilePointer::class.java)
            .apply {
                `when`(this.getPath()).thenReturn(path)
                `when`(this.getExtension()).thenReturn(extension)
                `when`(this.getNameWithoutExtension()).thenReturn(name)
            }
    }
}
