package bke.iso.v2.engine.asset

import bke.iso.engine.FilePointer
import bke.iso.engine.asset.AssetLoader
import bke.iso.v2.engine.Game
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.lang.IllegalArgumentException

class AssetsTest : StringSpec({
    val game = mockk<Game>()

    "should throw exception when add duplicate loader" {
        class LoaderA : AssetLoader<String> {
            override fun load(file: FilePointer): Pair<String, String> =
                "test" to "test"
        }

        class LoaderB : AssetLoader<String> {
            override fun load(file: FilePointer): Pair<String, String> =
                "test" to "test"
        }

        val exception = shouldThrow<IllegalArgumentException> {
            val assets = Assets(game)
            assets.addLoader("txt", LoaderA())
            assets.addLoader("txt", LoaderB())
        }
        exception.message shouldBe "Extension 'txt' has already been set to loader LoaderA"
    }
})
