package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.editor.createReferenceActor
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World

class CreateTileCommand(
    private val world: World,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand {

    private lateinit var actor: Actor

    override fun execute() {
        actor = createReferenceActor(world, location, prefab)
    }

    override fun undo() {
        world.actors.delete(actor)
    }
}
