package bke.iso.asset.test

import bke.iso.util.FilePointer
import bke.iso.asset.AssetLoader
import bke.iso.asset.BaseAssetLoader
import kotlin.reflect.KClass

internal data class Document(
    val name: String,
    val text: String
)

@AssetLoader(["txt", "doc"])
internal class DocumentLoader : BaseAssetLoader<Document>() {
    override fun loadAsset(file: FilePointer): Document =
        Document("doc1", "some text")

    override fun getAssetType(): KClass<Document> =
        Document::class
}
