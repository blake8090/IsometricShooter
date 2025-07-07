package bke.iso.editor.scene.view

import bke.iso.editor.EditorModule
import bke.iso.editor.entity.command.UpdateComponentPropertyCommand
import bke.iso.editor.entity.command.UpdateVector3Command
import bke.iso.editor.scene.EntityTemplateReference
import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.core.Events
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import imgui.ImGui
import imgui.ImVec2
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

        if (ImGui.treeNodeEx("Position##inspectorPos", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.inputFloat("x", ImFloat(entity.x))
            ImGui.inputFloat("y", ImFloat(entity.y))
            ImGui.inputFloat("z", ImFloat(entity.z))
            ImGui.treePop()
        }

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
        ImGui.separatorText("Template")

        val selectedTemplate = entity
            .get<EntityTemplateReference>()
            ?.template

        if (selectedTemplate != null) {
            ImGui.inputText(
                /* label = */ "Name##inspectorTemplateName",
                /* text = */ ImString(selectedTemplate),
                /* imGuiInputTextFlags = */ ImGuiInputTextFlags.ReadOnly
            )

            val template = assets.get<EntityTemplate>(selectedTemplate)
            for (component in template.components) {

                if (ImGui.treeNode(component::class.simpleName)) {
                    ImGui.text("Override")
                    ImGui.sameLine()
                    ImGui.checkbox("##inspectorTemplateOverride", false)
                    ImGui.sameLine()
                    ImGui.button("Revert##inspectorTemplateRevert")

                    ImGui.beginDisabled()
                    drawControls(component)
                    ImGui.endDisabled()

                    ImGui.treePop()
                }
            }
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
                else -> log.warn { "Could not generate controls for component ${component::class.simpleName} - KType ${memberProperty.returnType}" }
            }
        }
    }

    private fun drawStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val isMutable = memberProperty is KMutableProperty<*>

        if (!isMutable) {
            ImGui.beginDisabled()
        }

        val value = ImString(memberProperty.getter.call(component).toString(), inputTextLength)
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
        val value = ImFloat(memberProperty.getter.call(component) as Float)
        if (ImGui.inputFloat(memberProperty.name, value)) {
            val command =
                UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawBooleanControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImBoolean(memberProperty.getter.call(component) as Boolean)
        if (ImGui.checkbox(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawIntControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImInt(memberProperty.getter.call(component) as Int)
        if (ImGui.inputInt(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(EditorModule.ExecuteCommand(command))
        }
    }

    private fun drawVector3Controls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.collapsingHeader(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val vector = Vector3::class.cast(memberProperty.getter.call(component))

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
        }
    }

    private fun drawColorControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.collapsingHeader(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val color = Color::class.cast(memberProperty.getter.call(component))
            ImGui.inputFloat("r##${memberProperty.name}", ImFloat(color.r))
            ImGui.inputFloat("g##${memberProperty.name}", ImFloat(color.g))
            ImGui.inputFloat("b##${memberProperty.name}", ImFloat(color.b))
            ImGui.inputFloat("a##${memberProperty.name}", ImFloat(color.a))
        }
    }
}
