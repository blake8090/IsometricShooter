package bke.iso.v2.engine.render

import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.event.Event
import com.badlogic.gdx.graphics.g2d.SpriteBatch

data class DrawEntityEvent(
    val entity: Entity,
    val batch: SpriteBatch
) : Event()
