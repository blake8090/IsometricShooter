package bke.iso.engine.world

import bke.iso.engine.render.Sprite

data class Tile(val sprite: Sprite) : WorldObject() {
    override val layer = 0
}