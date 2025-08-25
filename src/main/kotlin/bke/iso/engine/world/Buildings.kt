package bke.iso.engine.world

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3

class Buildings(private val collisionBoxes: CollisionBoxes) {

    private val objectsByBuilding = mutableMapOf<String, MutableSet<Entity>>()
    private val buildingByObject = mutableMapOf<Entity, String>()
    private val boundsByBuilding = mutableMapOf<String, Box>()

    fun add(entity: Entity, buildingName: String) {
        require(buildingName.isNotBlank()) {
            "Building name cannot be blank"
        }

        objectsByBuilding
            .getOrPut(buildingName) { mutableSetOf() }
            .add(entity)
        buildingByObject[entity] = buildingName

        regenerateBounds()
    }

    fun getBounds(buildingName: String): Box? =
        boundsByBuilding[buildingName]

    private fun regenerateBounds() {
        for (building in objectsByBuilding.keys) {
            regenerateBounds(building)
        }
    }

    private fun regenerateBounds(buildingName: String) {
        val boxes = objectsByBuilding[buildingName]
            ?.mapNotNull(collisionBoxes::get)
            ?: emptyList()

        if (boxes.isEmpty()) {
            return
        }

        val min = Vector3(
            boxes.minOf { box -> box.min.x },
            boxes.minOf { box -> box.min.y },
            boxes.minOf { box -> box.min.z }
        )
        val max = Vector3(
            boxes.maxOf { box -> box.max.x },
            boxes.maxOf { box -> box.max.y },
            boxes.maxOf { box -> box.max.z },
        )

        boundsByBuilding[buildingName] = Box.fromMinMax(min, max)
    }

    fun getBuilding(entity: Entity): String? =
        buildingByObject[entity]

    fun getAll(): Set<String> =
        objectsByBuilding.keys

    fun remove(entity: Entity) {
        buildingByObject.remove(entity)
        for ((_, objects) in objectsByBuilding) {
            objects.remove(entity)
        }
        regenerateBounds()
    }

    fun clear() {
        objectsByBuilding.clear()
        buildingByObject.clear()
        boundsByBuilding.clear()
    }
}
