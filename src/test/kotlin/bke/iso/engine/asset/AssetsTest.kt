package bke.iso.engine.asset

import bke.iso.engine.SystemInfo
import bke.iso.engine.asset.cache.AssetCache
import bke.iso.engine.asset.cache.LoadedAsset
import bke.iso.engine.file.Files
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.io.File

class AssetsTest : StringSpec({
    val files = mockk<Files>()
    val systemInfo = mockk<SystemInfo>()

    // TODO: test multiple extensions

    "should throw exception when duplicate loader registered" {
        class LoaderA : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")


            override suspend fun loadAssets(file: File): List<LoadedAsset<String>> =
                emptyList()
        }

        class LoaderB : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")

            override suspend fun loadAssets(file: File) =
                listOf(LoadedAsset("test", "test"))
        }

        val exception = shouldThrow<IllegalStateException> {
            val assets = Assets(files, systemInfo)
            assets.register(LoaderA())
            assets.register(LoaderB())
        }
        exception.message shouldBe "Extension 'txt' already registered to LoaderA"
    }

    "should throw exception when cache not found" {
        val exception = shouldThrow<IllegalStateException> {
            val assets = Assets(files, systemInfo)
            assets.get<String>("test")
        }
        exception.message shouldBe "Expected asset cache for type String"
    }

    "should load files asynchronously" {
        class LoaderA : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")

            override suspend fun loadAssets(file: File) =
                listOf(LoadedAsset(file.name, "${file.name}: test"))
        }
        runBlocking {
            val file = mockk<File>()
            coEvery { file.name } returns "file.txt"
            coEvery { file.canonicalPath } returns "some/path/file.txt"

            val file2 = mockk<File>()
            coEvery { file2.name } returns "file2.txt"
            coEvery { file2.canonicalPath } returns "some/path/file2.txt"

            val path = "path"
            val fullPath = "$BASE_PATH/$path"
            coEvery { files.listFiles(fullPath) } returns sequenceOf(file, file2)
            coEvery { files.combinePaths(BASE_PATH, path) } returns fullPath

            val assets = Assets(files, systemInfo)
            assets.register(LoaderA())

            assets.loadAsync(path)
            assets.get<String>("file.txt") shouldBe "file.txt: test"
            assets.get<String>("file2.txt") shouldBe "file2.txt: test"
        }
    }

    "should return true when asset is present" {
        val expectedValue = "value"

        class LoaderA : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")

            override suspend fun loadAssets(file: File) =
                listOf(LoadedAsset("name", expectedValue))
        }

        runBlocking {
            val file = mockk<File>()
            coEvery { file.name } returns "file.txt"
            coEvery { file.canonicalPath } returns "some/path/file.txt"

            val path = "path"
            val fullPath = "$BASE_PATH/$path"
            coEvery { files.listFiles(fullPath) } returns sequenceOf(file)
            coEvery { files.combinePaths(BASE_PATH, path) } returns fullPath

            val assets = Assets(files, systemInfo)
            assets.register(LoaderA())

            assets.loadAsync(path)
            assets.contains(expectedValue) shouldBe true
        }
    }

    "should return false when asset is not present" {
        val value = "value"

        class LoaderA : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")

            override suspend fun loadAssets(file: File) =
                listOf(LoadedAsset("name", value))
        }

        runBlocking {
            val file = mockk<File>()
            coEvery { file.name } returns "file.txt"
            coEvery { file.canonicalPath } returns "some/path/file.txt"

            val path = "path"
            val fullPath = "$BASE_PATH/$path"
            coEvery { files.listFiles(fullPath) } returns sequenceOf(file)
            coEvery { files.combinePaths(BASE_PATH, path) } returns fullPath

            val assets = Assets(files, systemInfo)
            assets.register(LoaderA())

            assets.loadAsync(path)
            assets.contains("some other value") shouldBe false
        }
    }

    "should return false when no assets of that type were loaded" {
        val value = "value"

        class LoaderA : AssetCache<String>() {
            override val extensions: Set<String> = setOf("txt")

            override suspend fun loadAssets(file: File) =
                listOf(LoadedAsset("name", value))
        }

        runBlocking {
            val file = mockk<File>()
            coEvery { file.name } returns "file.txt"
            coEvery { file.canonicalPath } returns "some/path/file.txt"

            val path = "path"
            val fullPath = "$BASE_PATH/$path"
            coEvery { files.listFiles(fullPath) } returns sequenceOf(file)
            coEvery { files.combinePaths(BASE_PATH, path) } returns fullPath

            val assets = Assets(files, systemInfo)
            assets.register(LoaderA())

            assets.loadAsync(path)
            assets.contains(555f) shouldBe false
        }
    }
})
