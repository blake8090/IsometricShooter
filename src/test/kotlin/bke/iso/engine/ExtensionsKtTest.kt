package bke.iso.engine

import bke.iso.engine.math.sub2
import com.badlogic.gdx.math.Vector3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ExtensionsKtTest : StringSpec({
    "when subtracting vectors, should avoid precision errors" {
        val a = Vector3(0.2f, 0.1f, 5.4f)
        val b = Vector3(0.2f, 0.2f, 4.0f)

        val result = a.sub2(b)
        result shouldBe Vector3(0f, -0.1f, 1.4f)
    }
})
