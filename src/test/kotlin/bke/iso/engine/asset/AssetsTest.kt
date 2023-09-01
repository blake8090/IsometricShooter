package bke.iso.engine.asset

import bke.iso.engine.Game
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import java.io.File
import java.lang.IllegalArgumentException

class AssetsTest : StringSpec({
    val game = mockk<Game>()

    "should throw exception when add duplicate loader" {
        class LoaderA : AssetLoader<String> {
            override fun load(file: File): String =
                "test"
        }

        class LoaderB : AssetLoader<String> {
            override fun load(file: File): String =
                "test"
        }

        val exception = shouldThrow<IllegalArgumentException> {
            val assets = Assets(game)
            assets.addLoader("txt", LoaderA())
            assets.addLoader("txt", LoaderB())
        }
        exception.message shouldBe "Extension 'txt' has already been set to loader LoaderA"
    }
})
