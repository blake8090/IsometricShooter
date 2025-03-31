package bke.iso.editor.v3.actor.view

import bke.iso.editor.actor.ui.onChanged
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.v2.UIView
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ComponentInspectorView(
    private val skin: Skin,
    private val assets: Assets
) : UIView() {
    private val log = KotlinLogging.logger { }

    private lateinit var editorTable: Table

    override fun create() {
        setup()

        root.top().left()
        root.background = skin.getDrawable("bg")

        root.add(Label("Component Editor", skin))
            .left()

        root.row()
        editorTable = Table()
        root.add(editorTable)
            .growX()
            .padTop(Value.percentHeight(0.01f, root))
            .padRight(Value.percentWidth(0.01f, root))
    }

    private fun setup() {
        skin.add("actorInspectorView", ImageButton.ImageButtonStyle().apply {
            imageChecked = TextureRegionDrawable(TextureRegion(assets.get<Texture>("checkbox.png")))
            imageUp = TextureRegionDrawable(TextureRegion(assets.get<Texture>("unchecked.png")))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
        })
    }

    fun update(selectedComponent: Component) {
        editorTable.clearChildren()
        generateControls(selectedComponent)
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
                    updateComponentProperty(
                        actor = this,
                        component = component,
                        memberProperty = memberProperty,
                        value = text.toFloatOrNull() ?: 0f
                    )
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
                    updateComponentProperty(
                        actor = this,
                        component = component,
                        memberProperty = memberProperty,
                        value = text.toIntOrNull() ?: 0f
                    )
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
                    updateComponentProperty(
                        actor = this,
                        component = component,
                        memberProperty = memberProperty,
                        value = isChecked
                    )
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
                    if (memberProperty is KMutableProperty<*> && validateNewValue(memberProperty, text)) {
                        updateComponentProperty(
                            actor = this,
                            component = component,
                            memberProperty = memberProperty,
                            value = text
                        )
                    }
                }
            })
            .growX()
    }

    private fun validateNewValue(memberProperty: KProperty1<out Component, *>, value: String) =
        if (memberProperty.name != "texture") {
            true
        } else {
            assets.contains(value, Texture::class)
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
                    fire(OnVector3Changed(vector, x = text.toFloatOrNull() ?: 0f))
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
                    fire(OnVector3Changed(vector, y = text.toFloatOrNull() ?: 0f))
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
                    fire(OnVector3Changed(vector, z = text.toFloatOrNull() ?: 0f))
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
            actor.fire(OnComponentPropertyChanged(component, memberProperty as KMutableProperty1, value))
        }
    }

    data class OnVector3Changed(
        val vector3: Vector3,
        val x: Float = vector3.x,
        val y: Float = vector3.y,
        val z: Float = vector3.z
    ) : Event()

    data class OnComponentPropertyChanged(
        val component: Component,
        val memberProperty: KMutableProperty1<out Component, *>,
        val value: Any
    ) : Event()
}
