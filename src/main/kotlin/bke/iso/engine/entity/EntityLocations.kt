package bke.iso.engine.entity

import bke.iso.engine.Location
import bke.iso.engine.log
import java.util.*

class EntityLocations {
    private val locationById = mutableMapOf<UUID, Location>()
    private val idsByLocation = mutableMapOf<Location, MutableSet<UUID>>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )

    operator fun get(id: UUID): Location? =
        locationById[id]

    operator fun get(location: Location): Set<UUID> =
        idsByLocation[location] ?: emptySet()

    operator fun set(id: UUID, location: Location) {
        val previousLocation = locationById[id]
        if (previousLocation != null && previousLocation != location) {
            idsByLocation[previousLocation]?.remove(id)
            log.trace("Moving entity $id from $previousLocation to $location")
        }

        idsByLocation.getOrPut(location) { mutableSetOf() }
            .add(id)
        locationById[id] = location
    }

    fun getSortedIds(): Set<UUID> =
        idsByLocation
            .flatMap { entry -> entry.value }
            .toSet()
}
