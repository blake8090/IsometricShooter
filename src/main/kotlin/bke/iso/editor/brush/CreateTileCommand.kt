package bke.iso.editor.brush

import bke.iso.editor.EditorCommand
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World

class CreateTileCommand(
    private val world: World,
    private val prefab: TilePrefab,
    private val location: Location
) : EditorCommand {

    override fun execute() {
        world.setTile(location, Sprite(prefab.texture, 0f, 16f))
    }

    override fun undo() {
        TODO("Not yet implemented")
    }
}
