package bke.iso.engine.physics.collision

import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BoxTest {

    @Test
    fun `when getMin, then return min`() {
        val box = Box(Vector3(0.5f, 0.5f, 0f), 1f, 1f, 1f)
        assertThat(box.min).isEqualTo(Vector3(0f, 0f, 0f))
    }

    @Test
    fun `when getMax, then return max`() {
        val box = Box(Vector3(0.5f, 0.5f, 0f), 1f, 1f, 1f)
        assertThat(box.max).isEqualTo(Vector3(1f, 1f, 1f))
    }
}
