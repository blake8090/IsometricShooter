package bke.iso.engine.world

import bke.iso.engine.render.Sprite

data class Tile(
    val sprite: Sprite,
    val solid: Boolean = false
) : WorldObject() {

    override val layer = 0
}
