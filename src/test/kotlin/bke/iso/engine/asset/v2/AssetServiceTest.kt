package bke.iso.engine.asset.v2

import bke.iso.engine.FilePointer
import bke.iso.engine.FileService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.lang.IllegalArgumentException
import kotlin.io.path.Path

class AssetServiceTest : StringSpec({
    val fileService = mockk<FileService>()

    "should throw exception for duplicate loaders" {
        class StringLoader : AssetLoader<String> {
            override fun load(file: FilePointer): Pair<String, String> =
                TODO()
        }

        class AnotherStringLoader : AssetLoader<String> {
            override fun load(file: FilePointer): Pair<String, String> =
                TODO()
        }

        shouldThrow<IllegalArgumentException> {
            val assetService = AssetService(fileService)
            assetService.addLoader("txt", StringLoader())
            assetService.addLoader("txt", AnotherStringLoader())
        }
    }

    "should throw exception if module not loaded" {
        shouldThrow<IllegalStateException> {
            AssetService(fileService).get<String>("some-string")
        }
    }

    "should load and return assets" {
        val path = Path(ASSETS_DIRECTORY, "test")
        val file = mockFilePointer("images\\img01.jpg", "img01", "jpg")
        val file2 = mockFilePointer("images\\img02.png", "img02", "png")
        every { fileService.getFiles(path.toString()) } returns listOf(file, file2)

        class Image

        class ImageLoader : AssetLoader<Image> {
            override fun load(file: FilePointer): Pair<String, Image> =
                file.getNameWithoutExtension() to Image()
        }

        val assetService = AssetService(fileService)
        assetService.addLoader("jpg", ImageLoader())
        assetService.addLoader("png", ImageLoader())
        assetService.loadModule("test")

        assetService.get<Image>("img01") shouldNotBe null
        assetService.get<Image>("img02") shouldNotBe null
    }

    "should unload module when loading another" {
        val path = Path(ASSETS_DIRECTORY, "module1")
        val file = mockFilePointer("images\\img01.jpg", "img01", "jpg")
        every { fileService.getFiles(path.toString()) } returns listOf(file)

        val path2 = Path(ASSETS_DIRECTORY, "module2")
        val file2 = mockFilePointer("images\\img02.jpg", "img02", "jpg")
        every { fileService.getFiles(path2.toString()) } returns listOf(file2)

        class Image

        class ImageLoader : AssetLoader<Image> {
            override fun load(file: FilePointer): Pair<String, Image> =
                file.getNameWithoutExtension() to Image()
        }

        val assetService = AssetService(fileService)
        assetService.addLoader("jpg", ImageLoader())

        assetService.loadModule("module1")
        assetService.get<Image>("img01") shouldNotBe null
        assetService.get<Image>("img02") shouldBe null

        assetService.loadModule("module2")
        assetService.get<Image>("img01") shouldBe null
        assetService.get<Image>("img02") shouldNotBe  null
    }
})

private fun mockFilePointer(path: String, name: String, extension: String): FilePointer =
    mockk<FilePointer>().apply {
        every { getPath() } returns path
        every { getNameWithoutExtension() } returns name
        every { getExtension() } returns extension
    }