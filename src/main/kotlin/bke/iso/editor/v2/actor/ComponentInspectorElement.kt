package bke.iso.editor.v2.actor

import bke.iso.editor.v2.EditorState
import bke.iso.editor.v2.core.EditorCommand
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIElement
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
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ComponentInspectorElement(
    skin: Skin,
    private val assets: Assets
) : UIElement(skin) {

    private val log = KotlinLogging.logger { }

    private val root = Table().apply {
        top()
        left()
    }

    private lateinit var editorTable: Table

    override fun create(): Actor {
        setup()

        root.background = skin.getDrawable("bg")

        root.add(Label("Component Editor", skin))
            .left()

        root.row()
        editorTable = Table()
        root.add(editorTable)
            .growX()
            .padTop(Value.percentHeight(0.01f, root))
            .padRight(Value.percentWidth(0.01f, root))

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

    fun update(component: Component) {
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
            .padRight(Value.percentWidth(0.05f, editorTable))

        val currentValue = memberProperty.getter.call(component).toString()

        editorTable.add(
            TextField(currentValue, skin).apply {
                onChanged {
                    updateComponentProperty(this, component, memberProperty, text.toFloatOrNull() ?: 0f)
                }
            })
            .growX()
    }

    private fun generateIntControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(Value.percentWidth(0.05f, editorTable))

        val currentValue = memberProperty.getter.call(component).toString()

        editorTable.add(
            TextField(currentValue, skin).apply {
                onChanged {
                    updateComponentProperty(this, component, memberProperty, text.toIntOrNull() ?: 0f)
                }
            })
            .growX()
    }

    private fun generateBooleanControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()

        editorTable.add(
            ImageButton(skin, "actorInspectorView").apply {
                isChecked = memberProperty.getter.call(component) as Boolean == true
                onChanged {
                    updateComponentProperty(this, component, memberProperty, isChecked)
                }
            })
            .expandX()
            .right()
    }

    private fun generateStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(Value.percentWidth(0.05f, editorTable))

        val currentValue = memberProperty.getter.call(component).toString()

        editorTable.add(
            TextField(currentValue, skin).apply {
                if (memberProperty !is KMutableProperty<*>) {
                    isDisabled = true
                }

                onChanged {
                    updateComponentProperty(this, component, memberProperty, text)
                }
            })
            .growX()
    }

    private fun generateVector3Controls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()

        val vector = Vector3::class.cast(memberProperty.getter.call(component))

        editorTable.row()
        editorTable.add(Label("x", skin))
            .right()
            .padRight(Value.percentWidth(0.05f, editorTable))

        editorTable.add(
            TextField(vector.x.toString(), skin).apply {
                onChanged {
                    fireCommand(this, UpdateVectorXCommand(vector, text.toFloatOrNull() ?: 0f))
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("y", skin))
            .right()
            .padRight(Value.percentWidth(0.05f, editorTable))

        editorTable.add(
            TextField(vector.y.toString(), skin).apply {
                onChanged {
                    fireCommand(this, UpdateVectorYCommand(vector, text.toFloatOrNull() ?: 0f))
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("z", skin))
            .right()
            .padRight(Value.percentWidth(0.05f, editorTable))

        editorTable.add(
            TextField(vector.z.toString(), skin).apply {
                onChanged {
                    fireCommand(this, UpdateVectorZCommand(vector, text.toFloatOrNull() ?: 0f))
                }
            })
            .growX()
    }

    private fun updateComponentProperty(
        actor: Actor,
        component: Component,
        memberProperty: KProperty1<out Component, *>,
        value: Any
    ) {
        if (memberProperty is KMutableProperty<*>) {
            fireCommand(actor, UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value))
        }
    }

    private fun fireCommand(actor: Actor, command: EditorCommand) {
        actor.fire(EditorState.ExecuteCommandEvent(command))
    }
}

fun <T : Actor> T.onChanged(action: T.() -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}
