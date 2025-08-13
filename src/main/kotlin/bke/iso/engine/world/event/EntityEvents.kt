package bke.iso.engine.world.event

import bke.iso.engine.core.Event
import bke.iso.engine.world.entity.Entity

data class EntityCreated(val entity: Entity) : Event

data class EntityDeleted(val entity: Entity) : Event

data class EntityGridLocationChanged(val entity: Entity) : Event
