package bke.iso.engine.physics.collision

import com.badlogic.gdx.math.Vector3

data class Box(
    val center: Vector3,
    val width: Float,
    val length: Float,
    val height: Float
) {

    /**
     * Returns the minimum point of the box.
     *
     * This would be the bottom-left corner of the closest face.
     */
    fun getMin() =
        Vector3(
            center.x - (width / 2f),
            center.y - (length / 2f),
            center.z
        )

    /**
     * Returns the minimum point of the box.
     *
     * This would be the top-right corner of the farthest face.
     */
    fun getMax() =
        Vector3(
            center.x + (width / 2f),
            center.y + (length / 2f),
            center.z + height
        )
}
