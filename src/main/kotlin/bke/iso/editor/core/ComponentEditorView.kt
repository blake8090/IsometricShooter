package bke.iso.editor.core

import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImInt
import imgui.type.ImString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

private typealias ComponentProperty = KProperty1<out Component, *>

class ComponentEditorView(
    private val events: Events,
    private val assets: Assets
) {

    private val log = KotlinLogging.logger { }

    private val inputTextLength = 50
    private val selectedColorByLabel = mutableMapOf<String, FloatArray>()

    private val newKeyText = ImString("", inputTextLength)
    private val newValueText = ImString("", inputTextLength)

    fun draw(component: Component) {
        for (memberProperty in component::class.memberProperties) {
            when (memberProperty.returnType) {
                typeOf<Float>() -> drawFloatControls(component, memberProperty)
                typeOf<Float?>() -> drawFloatControls(component, memberProperty)
                typeOf<Int>() -> drawIntControls(component, memberProperty)
                typeOf<Boolean>() -> drawBooleanControls(component, memberProperty)
                typeOf<String>() -> drawStringControls(component, memberProperty)
                typeOf<Vector3>() -> drawVector3Controls(component, memberProperty)
                typeOf<Color>() -> drawColorControls(component, memberProperty)
                typeOf<PhysicsMode>() -> drawPhysicsModeControls(component, memberProperty)
                typeOf<MutableList<Vector3>>() -> {} // TODO: figure out how to handle these
                typeOf<MutableMap<String, String>>() -> drawMutableMapControls(component, memberProperty)
                else -> log.warn { "Could not generate controls for component ${component::class.simpleName} - property ${memberProperty.name} - KType ${memberProperty.returnType}" }
            }
        }
    }

    private fun drawStringControls(component: Component, memberProperty: ComponentProperty) {
        val isMutable = memberProperty is KMutableProperty<*>

        if (!isMutable) {
            ImGui.beginDisabled()
        }

        val value = ImString(get<String>(memberProperty, component), inputTextLength)
        if (ImGui.inputText(memberProperty.name, value)) {
            if (memberProperty is KMutableProperty<*>) {
                if (memberProperty.name != "texture" || assets.contains(value.toString(), Texture::class)) {
                    events.fire(
                        PropertyUpdated(
                            component,
                            memberProperty as KMutableProperty1,
                            value.toString()
                        )
                    )
                }
            }
        }

        if (!isMutable) {
            ImGui.endDisabled()
        }
    }

    private fun drawFloatControls(component: Component, memberProperty: ComponentProperty) {
        val isMutable = memberProperty is KMutableProperty<*>

        if (!isMutable) {
            ImGui.beginDisabled()
        }

        val value = ImFloat(get<Float>(memberProperty, component))
        if (ImGui.inputFloat(memberProperty.name, value)) {
            if (isMutable) {
                val event = PropertyUpdated(component, memberProperty as KMutableProperty1, value.get())
                events.fire(event)
            }
        }

        if (!isMutable) {
            ImGui.endDisabled()
        }
    }

    private fun drawBooleanControls(component: Component, memberProperty: ComponentProperty) {
        val value = ImBoolean(get<Boolean>(memberProperty, component))
        if (ImGui.checkbox(memberProperty.name, value)) {
            val command = PropertyUpdated(component, memberProperty as KMutableProperty1, value.get())
            events.fire(command)
        }
    }

    private fun drawIntControls(component: Component, memberProperty: ComponentProperty) {
        val value = ImInt(get<Int>(memberProperty, component))
        if (ImGui.inputInt(memberProperty.name, value)) {
            val command = PropertyUpdated(component, memberProperty as KMutableProperty1, value.get())
            events.fire(command)
        }
    }

    private fun drawVector3Controls(component: Component, memberProperty: ComponentProperty) {
        if (ImGui.treeNodeEx(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val vector = get<Vector3>(memberProperty, component)

            for (vectorProperty in Vector3::class.memberProperties) {
                val float = ImFloat(vectorProperty.getter.call(vector) as Float)
                if (ImGui.inputFloat("${vectorProperty.name}##vectorX", float)) {
                    val command = PropertyUpdated(vector, vectorProperty as KMutableProperty1, float.get())
                    events.fire(command)
                }
            }

            ImGui.treePop()
        }
    }

    private fun drawColorControls(component: Component, property: ComponentProperty) {
        if (ImGui.treeNodeEx(property.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val color = get<Color>(property, component)

            val label = "##${component.hashCode()}${property.name}colorEdit"
            val selectedColor = selectedColorByLabel
                .getOrPut(label, { floatArrayOf(color.r, color.g, color.b, color.a) })
            val refColor = floatArrayOf(color.r, color.g, color.b, color.a)
            ImGui.colorPicker4(label, selectedColor, refColor)

            if (ImGui.button("Apply")) {
                val newColor = Color(selectedColor[0], selectedColor[1], selectedColor[2], selectedColor[3])
                val command = PropertyUpdated(component, property as KMutableProperty1, newColor)
                events.fire(command)
            }

            ImGui.treePop()
        }
    }

    private fun drawPhysicsModeControls(component: Component, property: ComponentProperty) {
        val value = get<PhysicsMode>(property, component)
        ImGui.inputText(property.name, ImString(value.toString()))
    }

    private fun drawMutableMapControls(component: Component, property: ComponentProperty) {
        if (ImGui.treeNodeEx(property.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val map = get<MutableMap<String, String>>(property, component)

            val removedKeys = mutableSetOf<String>()
            map.forEach { (key, value) ->
                ImGui.beginGroup()

                ImGui.text("Key")
                ImGui.sameLine()
                ImGui.inputText("##${property.name}-$key", ImString(key))

                val valueString = ImString(value, inputTextLength)
                ImGui.text("Value")
                ImGui.sameLine()
                ImGui.inputText("##${property.name}-$value", valueString)

                if (ImGui.button("Delete##$key")) {
                    removedKeys.add(key)
                }

                ImGui.endGroup()
                ImGui.separator()
            }

            for (key in removedKeys) {
                events.fire(MapEntryRemoved(map, key))
            }

            ImGui.spacing()
            ImGui.text("Add New Entry:")

            ImGui.text("New Key")
            ImGui.sameLine()
            ImGui.inputText("##newKey", newKeyText)

            ImGui.text("New Value")
            ImGui.sameLine()
            ImGui.inputText("##newValue", newValueText)

            if (ImGui.button("Add Entry")) {
                val newKey = newKeyText.get()
                val newValue = newValueText.get()
                if (newKey.isNotEmpty() && newValue.isNotEmpty()) {
                    events.fire(MapEntryAdded(map, newKey, newValue))
                    newKeyText.set("")
                    newValueText.set("")
                }
            }

            ImGui.treePop()
        }
    }

    private inline fun <reified T : Any> get(property: ComponentProperty, instance: Component): T {
        return T::class.cast(property.getter.call(instance))
    }

    data class PropertyUpdated<T : Any>(
        val component: T,
        val property: KMutableProperty1<out T, *>,
        val newValue: Any
    ) : Event

    data class MapEntryAdded<Key : Any, Value : Any>(
        val map: MutableMap<Key, Value>,
        val key: Key,
        val value: Value
    ) : Event

    data class MapEntryRemoved<Key : Any, Value : Any>(
        val map: MutableMap<Key, Value>,
        val key: String
    ) : Event
}
