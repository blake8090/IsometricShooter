package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BoxTest : StringSpec({

    "when given pos and size should return box" {
        val pos = Vector3(0f, 0f, 4.7f)
        val size = Vector3(0.4f, 0.4f, 1.4f)

        val box = Box(pos, size)
        box.min.shouldBe(Vector3(-0.2f, -0.2f, 4f))
        box.max.shouldBe(Vector3(0.2f, 0.2f, 5.4f))
    }

    "when given pos and size should return box without precision errors" {
        val pos = Vector3()
        val size = Vector3(1f, 1f, 1f)

        val box = Box(pos, size)
        box.min.shouldBe(Vector3(-0.5f, -0.5f, -0.5f))
        box.max.shouldBe(Vector3(0.5f, 0.5f, 0.5f))
    }

    "when given min and max should return box" {
        val min = Vector3()
        val max = Vector3(2f, 2f, 2f)

        val box = Box.fromMinMax(min, max)
        box.pos.shouldBe(Vector3(1f, 1f, 1f))
        box.size.shouldBe(Vector3(2f, 2f, 2f))
        box.min.shouldBe(min)
        box.max.shouldBe(max)
    }

    "when given min and max should should return box without precision errors" {
        val min = Vector3(-0.2f, -0.2f, 4.0f)
        val max = Vector3(0.2f, 0.2f, 5.4f)

        val expected = Box(Vector3(0.0f, 0.0f, 4.7f), Vector3(0.4f, 0.4f, 1.4f))
        val result = Box.fromMinMax(min, max)

        result shouldBe expected
    }

    "when expand positive x axis should return box" {
        val pos = Vector3(0.5f, 0.5f, 0.5f)
        val size = Vector3(1f, 1f, 1f)

        val box = Box(pos, size).expand(1f, 0f, 0f)
        box.pos.shouldBe(Vector3(1f, 0.5f, 0.5f))
        box.size.shouldBe(Vector3(2f, 1f, 1f))
        box.min.shouldBe(Vector3(0f, 0f, 0f))
        box.max.shouldBe(Vector3(2f, 1f, 1f))
    }

    "when expand negative x axis should return box" {
        val pos = Vector3(0.5f, 0.5f, 0.5f)
        val size = Vector3(1f, 1f, 1f)

        val box = Box(pos, size).expand(-1f, 0f, 0f)
        box.pos.shouldBe(Vector3(0f, 0.5f, 0.5f))
        box.size.shouldBe(Vector3(2f, 1f, 1f))
        box.min.shouldBe(Vector3(-1f, 0f, 0f))
        box.max.shouldBe(Vector3(1f, 1f, 1f))
    }

    "when expand, should avoid precision errors" {
        val pos = Vector3(0f, 0f, 4.7f)
        val size = Vector3(0.4f, 0.4f, 1.4f)

        val box = Box(pos, size).expand(0f, 0f, -1f)
        box.pos.shouldBe(Vector3(0f, 0f, 4.2f))
        box.size.shouldBe(Vector3(0.4f, 0.4f, 2.4f))
        box.min.shouldBe(Vector3(-0.2f, -0.2f, 3f))
        box.max.shouldBe(Vector3(0.2f, 0.2f, 5.4f))
    }

    // TODO: write tests for other axes

    "when given segment return box" {
        val start = Vector3(0f, 0f, 0f)
        val end = Vector3(2f, 2f, 2f)

        val box = Box.fromMinMax(Segment(start, end))
        box.pos.shouldBe(Vector3(1f, 1f, 1f))
        box.size.shouldBe(Vector3(2f, 2f, 2f))
        box.min.shouldBe(start)
        box.max.shouldBe(end)
    }

    "when segment start > end should return box with correct size" {
        val start = Vector3(2f, 2f, 2f)
        val end = Vector3(0f, 0f, 0f)

        val box = Box.fromMinMax(Segment(start, end))
        box.pos.shouldBe(Vector3(1f, 1f, 1f))
        box.size.shouldBe(Vector3(2f, 2f, 2f))
        box.min.shouldBe(end)
        box.max.shouldBe(start)
    }
})
