package bke.iso.engine.render3d

import bke.iso.engine.asset.Assets
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy
import com.badlogic.gdx.graphics.g3d.decals.Decal
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class Renderer3D(
    private val assets: Assets,
    private val world: World
) {
    /*
    tiles -> decals (and later planes)
    actors -> decals rotated to camera
    walls -> model instance

    components:
        - sprite
        - boxModel
            - dimensions
            - color

    step 1: load scene from game map

    step 2: rendering
    map (actor to decal)
    map (wall actor to model instance)
    map (tile to decal)
     */

    private val camera = OrthographicCamera(20f, 10f)

    private val decalBatch = DecalBatch(CameraGroupStrategy(camera))
    private val modelBatch = ModelBatch()
    private val environment = Environment()

    private val decalByTile = mutableMapOf<Tile, Decal>()

    init {
        camera.apply {
            near = 1f
            far = 1000f
            position.set(3f, 3f, 5f)
            direction.set(0f, 0f, -1f)
            rotate(Vector3.X, 90f - 35.264f)
            rotate(Vector3.Z, 45f)

            // zoom out
            //translate(Vector3(direction).scl(-10f))
        }
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))
    }

    fun moveCamera(delta: Vector3) {
        camera.position.add(delta)
    }

    fun render(deltaTime: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.rotate(Vector3.X, -5f * deltaTime)
            println("cam rot x: ${camera.direction}, ${camera.up}")
        } else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.rotate(Vector3.X, 5f * deltaTime)
            println("cam rot x: ${camera.direction}, ${camera.up}")
        }
        camera.update()

        Gdx.gl.glClearColor(0f, 0f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST)

        for (gameObject in world.getObjects()) {
            if (gameObject is Tile) {
                val decal = decalByTile.getOrPut(gameObject) {
                    newDecal(gameObject.sprite.texture, gameObject.location.toVector3(), Vector2(1f, 1f))
                }
                decalBatch.add(decal)
            }
        }
        decalBatch.flush()

//        modelBatch.begin(camera)
//        modelBatch.render(rect, environment)
//        modelBatch.end()
    }

    private fun newDecal(texture: String, pos: Vector3, dim: Vector2): Decal {
        val textureRegion = TextureRegion(assets.get<Texture>(texture))
        val decal = Decal.newDecal(dim.x, dim.y, textureRegion)
        decal.setPosition(pos)
        return decal
    }

    fun dispose() {
        modelBatch.dispose()
        decalBatch.dispose()
    }
}
