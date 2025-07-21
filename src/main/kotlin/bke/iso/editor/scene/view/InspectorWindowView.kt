package bke.iso.editor.scene.view

import bke.iso.editor.EditorModule
import bke.iso.editor.entity.command.UpdateComponentPropertyCommand
import bke.iso.editor.entity.command.UpdateVector3Command
import bke.iso.editor.scene.EntityTemplateReference
import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.core.Events
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import imgui.ImColor
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiInputTextFlags
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

class InspectorWindowView(
    private val assets: Assets,
    private val events: Events
) {

    private val log = KotlinLogging.logger { }

    private val inputTextLength = 50

    private val componentOverrideColor = ImColor.rgb(50, 129, 168)

    fun draw(pos: ImVec2, size: ImVec2, data: SceneEditor.ViewData) {
        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Inspector##inspector")

        ImGui.beginDisabled(data.selectedEntity == null)

        val entity = data.selectedEntity
        if (entity != null) {
            drawMainProperties(entity, data)
            drawTemplateSection(entity)
        }

        ImGui.endDisabled()
        ImGui.end()
    }

    private fun drawMainProperties(entity: Entity, data: SceneEditor.ViewData) {
        ImGui.inputText("Id##inspectorId", ImString(entity.id), ImGuiInputTextFlags.ReadOnly)

        ImGui.inputFloat("x##inspectorPosX", ImFloat(entity.x))
        ImGui.inputFloat("y##inspectorPosY", ImFloat(entity.y))
        ImGui.inputFloat("z##inspectorPosZ", ImFloat(entity.z))

        if (ImGui.beginCombo("Building##inspectorBuilding", data.selectedBuilding)) {
            if (ImGui.selectable("None", data.selectedBuilding == null)) {
                events.fire(SceneEditor.BuildingAssigned(entity, null))
            }

            for (building in data.buildings) {
                if (ImGui.selectable(building, building == data.selectedBuilding)) {
                    events.fire(SceneEditor.BuildingAssigned(entity, building))
                }
            }

            ImGui.endCombo()
        }
    }

    private fun drawTemplateSection(entity: Entity) {
        ImGui.separatorText("")

        val templateReference = checkNotNull(entity.get<EntityTemplateReference>()) {
            "Expected entity $entity to have an EntityTemplateReference"
        }

        ImGui.inputText(
            /* label = */ "Template##inspectorTemplateName",
            /* text = */ ImString(templateReference.template),
            /* imGuiInputTextFlags = */ ImGuiInputTextFlags.ReadOnly
        )

        val template = assets.get<EntityTemplate>(templateReference.template)
        for (templateComponent in template.components) {
            val componentOverride = templateReference
                .componentOverrides
                .firstOrNull { c -> c::class == templateComponent::class }

            if (componentOverride == null) {
                drawTemplateComponent(templateReference, templateComponent)
            } else {
                drawComponentOverride(templateReference, componentOverride)
            }
        }
    }

    private fun drawTemplateComponent(templateReference: EntityTemplateReference, component: Component) {
        if (ImGui.checkbox("##inspectorComponentOverride-${component::class.simpleName}", false)) {
            events.fire(SceneEditor.ComponentOverrideEnabled(templateReference, component))
        }
        ImGui.setItemTooltip("Override Component")
        ImGui.sameLine()

        if (ImGui.collapsingHeader(component::class.simpleName, ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.beginDisabled()
            drawControls(component)
            ImGui.endDisabled()
        }
    }

    private fun drawComponentOverride(templateReference: EntityTemplateReference, component: Component) {
        if (ImGui.checkbox("##inspectorComponentOverride-${component::class.simpleName}", true)) {
            events.fire(SceneEditor.ComponentOverrideDisabled(templateReference, component))
        }
        ImGui.setItemTooltip("Override Component")
        ImGui.sameLine()

        ImGui.pushStyleColor(ImGuiCol.Header, componentOverrideColor)
        val open = ImGui.collapsingHeader(component::class.simpleName, ImGuiTreeNodeFlags.DefaultOpen)
        ImGui.popStyleColor()

        if (open) {
            drawControls(component)
        }
    }

    private fun drawControls(component: Component) {
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
                else -> log.warn { "Could not generate controls for component ${component::class.simpleName} - property ${memberProperty.name} - KType ${memberProperty.returnType}" }
            }
        }
    }

    private fun drawStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val isMutable = memberProperty is KMutableProperty<*>

        if (!isMutable) {
            ImGui.beginDisabled()
        }

        val value = ImString(get<String>(memberProperty, component), inputTextLength)
        if (ImGui.inputText(memberProperty.name, value)) {
            if (memberProperty is KMutableProperty<*>) {
                if (memberProperty.name != "texture" || assets.contains(value.toString(), Texture::class)) {
                    events.fire(
                        EditorModule.ExecuteCommand(
                            UpdateComponentPropertyCommand(
                                component = component,
                                property = memberProperty as KMutableProperty1,
                                newValue = value.toString()
                            )
                        )
                    )
                }
            }
        }

        if (!isMutable) {
            ImGui.endDisabled()
        }
    }

    private fun drawFloatControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImFloat(get<Float>(memberProperty, component))
        if (ImGui.inputFloat(memberProperty.name, value)) {
            val command =
                UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawBooleanControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImBoolean(get<Boolean>(memberProperty, component))
        if (ImGui.checkbox(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawIntControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImInt(get<Int>(memberProperty, component))
        if (ImGui.inputInt(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawVector3Controls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.treeNodeEx(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val vector = get<Vector3>(memberProperty, component)

            val xValue = ImFloat(vector.x)
            if (ImGui.inputFloat("x##${memberProperty.name}", xValue)) {
                if (xValue.get() != vector.x) {
                    val command = UpdateVector3Command(vector, xValue.get(), vector.y, vector.z)
                    events.fire(EditorModule.ExecuteCommand(command))
                }
            }

            val yValue = ImFloat(vector.y)
            if (ImGui.inputFloat("y##${memberProperty.name}", yValue)) {
                if (yValue.get() != vector.y) {
                    val command = UpdateVector3Command(vector, vector.x, yValue.get(), vector.z)
                    events.fire(EditorModule.ExecuteCommand(command))
                }
            }

            val zValue = ImFloat(vector.z)
            if (ImGui.inputFloat("z##${memberProperty.name}", zValue)) {
                if (zValue.get() != vector.z) {
                    val command = UpdateVector3Command(vector, xValue.get(), vector.y, zValue.get())
                    events.fire(EditorModule.ExecuteCommand(command))
                }
            }

            ImGui.treePop()
        }
    }

    private fun drawColorControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.treeNodeEx(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val color = get<Color>(memberProperty, component)
            // TODO: enable changes
            ImGui.inputFloat("r##${memberProperty.name}", ImFloat(color.r))
            ImGui.inputFloat("g##${memberProperty.name}", ImFloat(color.g))
            ImGui.inputFloat("b##${memberProperty.name}", ImFloat(color.b))
            ImGui.inputFloat("a##${memberProperty.name}", ImFloat(color.a))
            ImGui.treePop()
        }
    }

    private fun drawPhysicsModeControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = get<PhysicsMode>(memberProperty, component)
        ImGui.inputText(memberProperty.name, ImString(value.toString()))
    }

    private inline fun <reified T : Any> get(property: KProperty1<out Component, *>, instance: Component): T {
        return T::class.cast(property.getter.call(instance))
    }
}
