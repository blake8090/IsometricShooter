package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

data class Tile(
    val sprite: Sprite = Sprite(),
    val solid: Boolean = false,
    var location: Location = Location(),
    var selected: Boolean = false
) : GameObject
