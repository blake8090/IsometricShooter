package bke.iso.editor.actor.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ActorInspectorView(
    private val skin: Skin,
    private val assets: Assets
) {

    private val log = KotlinLogging.logger { }

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)

    private lateinit var editorTable: Table

    fun create(): Actor {
        setup()

        root.background = skin.getDrawable("bg")

        root.add(Label("Component Editor", skin))
            .left()

        root.row()
        editorTable = Table()
        root.add(editorTable)
            .expand()
            .top()
            .left()
            .padTop(5f)

        return root
    }

    private fun setup() {
        skin.add("actorInspectorView", ImageButton.ImageButtonStyle().apply {
            imageChecked = TextureRegionDrawable(TextureRegion(assets.get<Texture>("checkbox.png")))
            imageUp = TextureRegionDrawable(TextureRegion(assets.get<Texture>("unchecked.png")))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
        })
    }

    fun updateComponent(component: Component) {
        editorTable.clearChildren()
        generateControls(component)
    }

    private fun generateControls(component: Component) {
        for (memberProperty in component::class.memberProperties) {
            editorTable.row()
            when (memberProperty.returnType) {
                typeOf<Float>() -> generateFloatControls(component, memberProperty)
                typeOf<Float?>() -> generateFloatControls(component, memberProperty)
                typeOf<Int>() -> generateIntControls(component, memberProperty)
                typeOf<Boolean>() -> generateBooleanControls(component, memberProperty)
                typeOf<String>() -> generateStringControls(component, memberProperty)
                typeOf<Vector3>() -> generateVector3Controls(component, memberProperty)
                else -> log.warn { "Could not generate controls for component ${component::class.simpleName} - KType ${memberProperty.returnType}" }
            }
        }
    }

    private fun generateFloatControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        editorTable.add(
            TextField(memberProperty.getter.call(component).toString(), skin).apply {
                onChanged {
                    if (memberProperty is KMutableProperty<*>) {
                        memberProperty.setter.call(component, text.toFloatOrNull() ?: 0f)
                    }
                }
            })
            .growX()
    }

    private fun generateIntControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        editorTable.add(
            TextField(memberProperty.getter.call(component).toString(), skin).apply {
                onChanged {
                    if (memberProperty is KMutableProperty<*>) {
                        memberProperty.setter.call(component, text.toIntOrNull() ?: 0)
                    }
                }
            })
            .growX()
    }

    private fun generateBooleanControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        editorTable.add(
            ImageButton(skin, "actorInspectorView").apply {
                isChecked = memberProperty.getter.call(component) as Boolean == true
                onChanged {
                    if (memberProperty is KMutableProperty<*>) {
                        memberProperty.setter.call(component, isChecked)
                    }
                }
            })
            .growX()
    }

    private fun generateStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        editorTable.add(
            TextField(memberProperty.getter.call(component).toString(), skin).apply {
                if (memberProperty !is KMutableProperty<*>) {
                    isDisabled = true
                }

                onChanged {
                    if (memberProperty is KMutableProperty<*>) {
                        if (memberProperty.name != "texture" || assets.contains(text, Texture::class)) {
                            memberProperty.setter.call(component, text)
                        }
                    }
                }
            })
            .growX()
    }

    private fun generateVector3Controls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        val vector = Vector3::class.cast(memberProperty.getter.call(component))

        editorTable.row()
        editorTable.add(Label("x", skin))
            .right()
            .padRight(5f)

        editorTable.add(
            TextField(vector.x.toString(), skin).apply {
                onChanged {
                    vector.x = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("y", skin))
            .right()
            .padRight(5f)

        editorTable.add(
            TextField(vector.y.toString(), skin).apply {
                onChanged {
                    vector.y = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("z", skin))
            .right()
            .padRight(5f)

        editorTable.add(
            TextField(vector.z.toString(), skin).apply {
                onChanged {
                    vector.z = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()
    }
}

fun <T : Actor> T.onChanged(action: T.() -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}
