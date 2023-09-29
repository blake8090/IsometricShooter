package bke.iso.editor

import bke.iso.editor.main.EditorScreen
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Location
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.floor

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val cameraPanScale = Vector2(0.5f, 0.5f)
    private val referenceActor = game.world.actors.create(0f, 0f, 0f)
    private var selection: Selection? = null

    override suspend fun load() {
        log.info { "Starting editor" }
        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)
        game.input.addInputProcessor(mouseDragAdapter)
    }

    fun handleEvent(event: EditorEvent) =
        when (event) {
            is TilePrefabSelectedEvent -> {
                log.debug { "tile prefab '${event.prefab.name}' selected" }
                val sprite = Sprite(event.prefab.texture, 0f, 16f)
                referenceActor.add(sprite)
                selection = TileSelection(event.prefab)
            }

            is ActorPrefabSelectedEvent -> {
                log.debug { "actor prefab '${event.prefab.name}' selected" }
                referenceActor.add(event.sprite.copy())
                event.prefab.components
                    .filterIsInstance<Collider>()
                    .firstOrNull()
                    ?.let { referenceActor.add(it.copy()) }
                selection = ActorSelection(event.prefab)
            }
        }

    override fun update(deltaTime: Float) {
        drawGrid()
        panCamera()

        if (editorScreen.hitMainView()) {
            updateReferenceActor()
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                if (selection is TileSelection) {
                    createTile((selection as TileSelection).prefab, Location(referenceActor.pos))
                } else if (selection is ActorSelection) {
                    createActor((selection as ActorSelection).prefab, referenceActor.pos)
                }
            }
        }
    }

    private fun panCamera() {
        val delta = mouseDragAdapter.getDelta()
        if (delta.isZero) {
            return
        }
        val cameraDelta = Vector2(
            delta.x * cameraPanScale.x * -1, // for some reason the delta's x-axis is inverted!
            delta.y * cameraPanScale.y
        )
        game.renderer.moveCamera(cameraDelta)
    }

    private fun updateReferenceActor() {
        // TODO: scale position when screen size changes
        val pos = toWorld(game.renderer.getCursorPos())

        if (selection is TileSelection) {
            pos.set(floor(pos.x), floor(pos.y), floor(pos.z))
        }
        referenceActor.moveTo(pos.x, pos.y, pos.z)
    }

    private fun createTile(prefab: TilePrefab, location: Location) {
        log.debug { "set tile ${prefab.name} at $location" }
        game.world.setTile(location, Sprite(prefab.texture, 0f, 16f))
    }

    private fun createActor(prefab: ActorPrefab, pos: Vector3) {
        val sprite = referenceActor.get<Sprite>()!!
        val a = game.world.actors.create(
            pos.x, pos.y, pos.z,
            sprite.copy(),
            ReferencePrefab(prefab)
        )
        prefab.components.filterIsInstance<Collider>()
            .firstOrNull()?.let { a.add(it.copy()) }
    }

    private fun drawGrid() {
        for (x in 0..gridWidth) {
            game.renderer.shapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }

        for (y in 0..gridLength) {
            game.renderer.shapes.addLine(
                Vector3(0f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class ActorSelection(val prefab: ActorPrefab) : Selection()

    private data class ReferencePrefab(val prefab: ActorPrefab) : Component
}
