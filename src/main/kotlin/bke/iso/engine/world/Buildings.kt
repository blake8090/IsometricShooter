package bke.iso.engine.world

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Actor
import com.badlogic.gdx.math.Vector3

class Buildings {

    private val objectsByBuilding = mutableMapOf<String, MutableSet<Actor>>()
    private val buildingByObject = mutableMapOf<Actor, String>()
    private val boundsByBuilding = mutableMapOf<String, Box>()

    fun add(actor: Actor, buildingName: String) {
        require(buildingName.isNotBlank()) {
            "Building name cannot be blank"
        }

        objectsByBuilding
            .getOrPut(buildingName) { mutableSetOf() }
            .add(actor)
        buildingByObject[actor] = buildingName

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
            ?.mapNotNull(Actor::getCollisionBox)
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

    fun getBuilding(actor: Actor): String? =
        buildingByObject[actor]

    fun getAll(): Set<String> =
        objectsByBuilding.keys

    fun remove(actor: Actor) {
        buildingByObject.remove(actor)
        for ((_, objects) in objectsByBuilding) {
            objects.remove(actor)
        }
        regenerateBounds()
    }

    fun clear() {
        objectsByBuilding.clear()
        buildingByObject.clear()
        boundsByBuilding.clear()
    }
}
