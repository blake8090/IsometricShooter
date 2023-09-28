package bke.iso.engine.render3d

import bke.iso.engine.asset.Assets
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy
import com.badlogic.gdx.graphics.g3d.decals.Decal
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
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

    private val camera = OrthographicCamera(18f, 10f)

    private val decalBatch = DecalBatch(CameraGroupStrategy(camera))
    private val modelBatch = ModelBatch()
    private val environment = Environment()

    private val decalByActor = mutableMapOf<Actor, Decal>()
    private val boxModelByActor = mutableMapOf<Actor, ModelInstance>()
    private val modelByTile = mutableMapOf<Tile, ModelInstance>()

    init {
        camera.apply {
            near = 1f
            far = 1000f
            position.set(3f, 3f, 0f)
            direction.set(0f, 0f, -1f)
            rotate(Vector3.X, 90f - 35.264f)
            rotate(Vector3.Z, 45f)

            // zoom out
            translate(Vector3(direction).scl(-20f))
        }
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f))
        environment.add(DirectionalLight().set(Color.WHITE, Vector3(1f, 1f, -0.5f)))
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

        val instances = mutableListOf<ModelInstance>()
        for (gameObject in world.getObjects()) {
            if (gameObject is Tile) {
                val instance = modelByTile.getOrPut(gameObject) {
                    newRect(gameObject.sprite.texture)
                }
                instance.transform.setToTranslation(gameObject.location.toVector3().add(0f, 0f, 0.0001f))
                instances.add(instance)
            } else if (gameObject is Actor) {
                if (gameObject.has<BoxModel>()) {
                    val instance = boxModelByActor.getOrPut(gameObject) {
                        newBoxModel(gameObject.pos, Vector3(1f, 1f, 2f))
                    }
                    instances.add(instance)
                } else {
                    // TODO: use planes so that billboards are affected by lighting
                    gameObject.get<Billboard>()?.let { billboard ->
                        val decal = decalByActor.getOrPut(gameObject) {
                            newActorDecal(billboard.texture, gameObject.pos, Vector2(billboard.width, billboard.height))
                        }
                        decal.setPosition(gameObject.pos)
                        decal.setRotation(camera.direction.cpy().scl(-1f), Vector3.Z)
                        decalBatch.add(decal)
                    }
                }
            }
        }

        modelBatch.begin(camera)
        modelBatch.render(instances, environment)
        modelBatch.end()

        decalBatch.flush()
    }

    private fun newActorDecal(texture: String, pos: Vector3, dim: Vector2): Decal {
        val textureRegion = TextureRegion(assets.get<Texture>(texture))
        val decal = Decal.newDecal(dim.x, dim.y, textureRegion, true)
        decal.setPosition(pos)
        println("new decal $texture $pos")
        decal.transformationOffset = Vector2(
            dim.x / 2 * -1f,
            dim.y / 2 * -1f
        )
        return decal
    }

    private fun newBoxModel(pos: Vector3, size: Vector3): ModelInstance {
        val model = ModelBuilder().createBox(
            size.x, size.y, size.z,
            Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position.toLong().or(VertexAttributes.Usage.Normal.toLong())
        )
        val instance = ModelInstance(model)
        instance.transform.setToTranslation(pos.cpy().add(0f, 0f, 1f))
        return instance
    }

    private fun newRect(texture: String, width: Float = 1f, height: Float = 1f): ModelInstance {
        val builder = ModelBuilder()
        builder.begin()
        builder.node()
        val mpb: MeshPartBuilder = builder.part(
            "rect",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position.toLong().or(VertexAttributes.Usage.Normal.toLong().or(VertexAttributes.Usage.TextureCoordinates.toLong())),
            Material(TextureAttribute(TextureAttribute.Diffuse, assets.get<Texture>(texture)))
        )
        mpb.rect(
            -(width * 0.5f), -(height * 0.5f), 0f,
            (width * 0.5f), -(height * 0.5f), 0f,
            (width * 0.5f), (height * 0.5f), 0f,
            -(width * 0.5f), (height * 0.5f), 0f,
            0f, 0f, -1f
        )
        return ModelInstance(builder.end())
    }

    fun dispose() {
        modelBatch.dispose()
        decalBatch.dispose()
    }
}
