package bke.iso.engine.world

import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.Box
import com.badlogic.gdx.math.Vector3

class Buildings {

    private val objectsByBuilding = mutableMapOf<String, MutableSet<GameObject>>()

    fun add(gameObject: GameObject, buildingName: String) {
        objectsByBuilding
            .getOrPut(buildingName) { mutableSetOf() }
            .add(gameObject)
    }

    fun getBounds(buildingName: String): Box? {
        val objects = objectsByBuilding[buildingName] ?: return null
        val boxes = objects.mapNotNull(GameObject::getCollisionBox)

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

        return Box.fromMinMax(min, max)
    }

    fun getAll(): Set<String> =
        objectsByBuilding.keys
}
