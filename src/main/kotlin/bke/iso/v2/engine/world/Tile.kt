package bke.iso.v2.engine.world

import bke.iso.v2.engine.math.Location
import bke.iso.v2.engine.render.Sprite

data class Tile(
    val sprite: Sprite = Sprite(),
    val solid: Boolean = false,
    var location: Location = Location()
) : GameObject()
