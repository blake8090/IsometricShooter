package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlin.io.path.Path

class AssetModuleTest : StringSpec({
    val fileService = mockk<FileService>()

    "should throw exception if no files were found" {
        val path = Path(ASSETS_DIRECTORY, "test")
        every { fileService.getFiles(path.toString()) } returns emptyList()

        shouldThrow<IllegalStateException> {
            val module = AssetModule("test")
            module.load(fileService, mutableMapOf())
        }
    }

    "should throw exception when two assets have the same name and type" {
        val path = Path(ASSETS_DIRECTORY, "test")
        every { fileService.getFiles(path.toString()) } returns listOf(
            mockFilePointer("images\\img01.jpg", "img01", "jpg"),
            mockFilePointer("images\\img01.png", "img01", "png"),
            mockFilePointer("images\\img02.jpg", "img01", "jpg")
        )

        class Image

        class ImageLoader : AssetLoader<Image> {
            override fun load(file: FilePointer): Pair<String, Image> =
                file.getNameWithoutExtension() to Image()
        }

        val loaders = mutableMapOf(
            "jpg" to ImageLoader(),
            "png" to ImageLoader()
        )
        shouldThrow<IllegalStateException> {
            val module = AssetModule("test")
            module.load(fileService, loaders)
        }
    }

    "should load files and return assets" {
        val path = Path(ASSETS_DIRECTORY, "test")
        val file = mockFilePointer("images\\img01.jpg", "img01", "jpg")
        val file2 = mockFilePointer("images\\img02.jpg", "img02", "jpg")
        every { fileService.getFiles(path.toString()) } returns listOf(file, file2)

        class Image

        class ImageLoader : AssetLoader<Image> {
            override fun load(file: FilePointer): Pair<String, Image> =
                file.getNameWithoutExtension() to Image()
        }

        val module = AssetModule("test")
        module.load(fileService, mutableMapOf("jpg" to ImageLoader()))
        module.get<Image>("img01") shouldNotBe null
        module.get<Image>("img02") shouldNotBe null
    }
})