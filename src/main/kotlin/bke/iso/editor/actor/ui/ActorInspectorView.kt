package bke.iso.editor.actor.ui

import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ActorInspectorView(private val skin: Skin) {

    private val log = KotlinLogging.logger { }

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)

    private lateinit var editorTable: Table

    fun create(): Actor {
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

    private fun generateStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        editorTable.add(Label(memberProperty.name, skin))
            .left()
            .padRight(5f)

        editorTable.add(
            TextField(memberProperty.getter.call(component).toString(), skin).apply {
                onChanged {
                    if (memberProperty is KMutableProperty<*>) {
                        // make sure to validate texture
                        if (memberProperty.name != "texture") {
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

fun <T : TextField> T.onChanged(action: T.() -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}
