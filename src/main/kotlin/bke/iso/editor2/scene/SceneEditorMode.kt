package bke.iso.editor2.scene

import bke.iso.editor2.EditorMode
import bke.iso.engine.asset.Assets
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneEditorMode(
    renderer: Renderer,
    world: World,
    assets: Assets,
) : EditorMode(renderer, world) {

    private val log = KotlinLogging.logger { }

    private val view = SceneEditorView(assets)

    override fun update() {
    }

    override fun draw() {
        view.draw()
    }
}
