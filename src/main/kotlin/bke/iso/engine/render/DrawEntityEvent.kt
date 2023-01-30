package bke.iso.engine.render

import bke.iso.engine.entity.Entity
import bke.iso.engine.event.Event
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

data class DrawEntityEvent(
    val entity: Entity,
    val batch: PolygonSpriteBatch
) : Event()
