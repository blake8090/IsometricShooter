package bke.iso.engine.render3d

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3

data class BoxModel(val size: Vector3) : Component

data class Billboard(
    val texture: String,
    val width: Float,
    val height: Float
) : Component
