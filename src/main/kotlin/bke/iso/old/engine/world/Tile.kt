package bke.iso.old.engine.world

import bke.iso.old.engine.render.Sprite

data class Tile(
    val sprite: Sprite,
    val solid: Boolean = false
) : WorldObject() {

    override val layer = 0
}
