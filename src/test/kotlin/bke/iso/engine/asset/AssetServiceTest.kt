package bke.iso.engine.asset

import bke.iso.engine.di.ServiceContainer
import bke.iso.engine.util.FilePointer
import bke.iso.engine.util.FileService
import bke.iso.engine.util.ReflectionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.reflect.KClass

data class Document(val contents: String)

@AssetLoader("docs", ["txt"])
class DocumentLoader : BaseAssetLoader<Document>() {
    override fun loadAssets(files: List<FilePointer>): Map<String, Document> =
        mutableMapOf(
            "doc1" to Document("abc"),
            "doc2" to Document("def")
        )

    override fun getAssetType(): KClass<Document> = Document::class
}

data class Entity(val id: Int)

@AssetLoader("ent", ["ent"])
class EntityLoader : BaseAssetLoader<Entity>() {
    override fun loadAssets(files: List<FilePointer>): Map<String, Entity> =
        mutableMapOf(
            "ent1" to Entity(1),
            "ent2" to Entity(2)
        )

    override fun getAssetType(): KClass<Entity> = Entity::class
}

internal class AssetServiceTest {
    @Test
    fun `should return assets using a given list of asset loaders`() {
        val basePath = "data"

        val container = mock(ServiceContainer::class.java)
        `when`(container.createInstance(DocumentLoader::class)).thenReturn(DocumentLoader())
        `when`(container.createInstance(EntityLoader::class)).thenReturn(EntityLoader())

        val fileService = mock(FileService::class.java)
        val documentFiles = listOf(
            mockFile("$basePath/docs", "txt", "doc1"),
            mockFile("$basePath/docs", "txt", "doc2")
        )
        `when`(fileService.getFiles("$basePath/docs")).thenReturn(documentFiles)
        val entityFiles = listOf(
            mockFile("$basePath/ent", "txt", "ent1"),
            mockFile("$basePath/ent", "ent", "ent2")
        )
        `when`(fileService.getFiles("$basePath/ent")).thenReturn(entityFiles)

        val assetService = AssetService(container, fileService, mock(ReflectionService::class.java))
        assetService.setupAssetLoaders(setOf(DocumentLoader::class, EntityLoader::class))
        assetService.loadAssets(basePath)

        assertThat(assetService.getAsset("doc1", Document::class)).isEqualTo(Document("abc"))
        assertThat(assetService.getAsset("doc2", Document::class)).isEqualTo(Document("def"))
        assertThat(assetService.getAsset("ent1", Entity::class)).isEqualTo(Entity(1))
        assertThat(assetService.getAsset("ent2", Entity::class)).isEqualTo(Entity(2))
    }

    @Test
    fun `should return assets using all asset loaders in a package`() {
        val basePath = "data"
        val basePackage = "bke.io.asset"

        val container = mock(ServiceContainer::class.java)
        `when`(container.createInstance(DocumentLoader::class)).thenReturn(DocumentLoader())
        `when`(container.createInstance(EntityLoader::class)).thenReturn(EntityLoader())

        val fileService = mock(FileService::class.java)
        val documentFiles = listOf(
            mockFile("$basePath/docs", "txt", "doc1"),
            mockFile("$basePath/docs", "txt", "doc2")
        )
        `when`(fileService.getFiles("$basePath/docs")).thenReturn(documentFiles)
        val entityFiles = listOf(
            mockFile("$basePath/ent", "txt", "ent1"),
            mockFile("$basePath/ent", "ent", "ent2")
        )
        `when`(fileService.getFiles("$basePath/ent")).thenReturn(entityFiles)

        val reflectionService = mock(ReflectionService::class.java)
        `when`(reflectionService.findSubTypesWithAnnotation<BaseAssetLoader<*>, AssetLoader>(basePackage))
            .thenReturn(setOf(DocumentLoader::class, EntityLoader::class))

        val assetService = AssetService(container, fileService, reflectionService)
        assetService.setupAssetLoadersInPackage(basePackage)
        assetService.loadAssets(basePath)

        assertThat(assetService.getAsset("doc1", Document::class)).isEqualTo(Document("abc"))
        assertThat(assetService.getAsset("doc2", Document::class)).isEqualTo(Document("def"))
        assertThat(assetService.getAsset("ent1", Entity::class)).isEqualTo(Entity(1))
        assertThat(assetService.getAsset("ent2", Entity::class)).isEqualTo(Entity(2))
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
