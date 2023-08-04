package bke.iso.old.engine.render

import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.event.Event
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

data class DrawEntityEvent(
    val entity: Entity,
    val batch: PolygonSpriteBatch
) : Event()
