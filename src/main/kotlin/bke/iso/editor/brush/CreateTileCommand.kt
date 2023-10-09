package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.TilePrefabReference
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3

class CreateTileCommand(
    private val world: World,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand {

    override fun execute() {
        world.actors.create(
            location,
            Sprite(prefab.texture, 0f, 16f),
            TilePrefabReference(prefab.name),
            Collider(Vector3(1f, 1f, 0f))
        )
    }

    override fun undo() {
        TODO("Not yet implemented")
    }
}
