package bke.iso.engine.world

import com.badlogic.gdx.math.Vector3
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WorldGridTest {
    @Test
    fun `should set and return tiles`() {
        val floor = Tile("floor", false)
        val wall = Tile("wall", true)

        val worldGrid = WorldGrid()

        worldGrid.setTile(floor, Location(2, 3))
        assertThat(worldGrid.getTile(Location(2, 3))).isEqualTo(floor)

        worldGrid.setTile(wall, Location(2, 3))
        assertThat(worldGrid.getTile(Location(2, 3))).isEqualTo(wall)
    }

    @Test
    fun `should update entity location`() {
        val entity = 1

        val worldGrid = WorldGrid()

        worldGrid.updateEntityLocation(entity, Vector3(0f, 0f, 0f))
        assertThat(worldGrid.getEntityLocation(entity)).isEqualTo(Location(0, 0))

        worldGrid.updateEntityLocation(entity, Vector3(1f, 2f, 0f))
        assertThat(worldGrid.getEntityLocation(entity)).isEqualTo(Location(1, 2))
    }

    @Test
    fun `should return entities at location`() {
        val entity = 1
        val entity2 = 2
        val entity3 = 3

        val worldGrid = WorldGrid()
        worldGrid.updateEntityLocation(entity, Vector3(0f, 0f, 0f))
        worldGrid.updateEntityLocation(entity2, Vector3(1f, 0f, 0f))
        worldGrid.updateEntityLocation(entity3, Vector3(1f, 0f, 0f))

        assertThat(worldGrid.getEntityLocation(entity)).isEqualTo(Location(0, 0))
        assertThat(worldGrid.getEntityLocation(entity2)).isEqualTo(Location(1, 0))
        assertThat(worldGrid.getEntityLocation(entity3)).isEqualTo(Location(1, 0))
    }

    @Test
    fun `should handle entities with non integer positions`() {
        val entity = 1
        val entity2 = 2

        val worldGrid = WorldGrid()
        worldGrid.updateEntityLocation(entity, Vector3(0f, 1.999f, 0f))
        worldGrid.updateEntityLocation(entity2, Vector3(4.5f, 2.001f, 0f))

        assertThat(worldGrid.getEntityLocation(entity)).isEqualTo(Location(0, 1))
        assertThat(worldGrid.getEntityLocation(entity2)).isEqualTo(Location(4, 2))
    }
}
