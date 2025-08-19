package bke.iso.editor.scene.view

import bke.iso.editor.component.ComponentEditorView
import bke.iso.editor.scene.EntityData
import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.core.Events
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import imgui.ImColor
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImFloat
import imgui.type.ImString

class InspectorWindowView(
    assets: Assets,
    private val events: Events,
) {

    private val componentOverrideColor = ImColor.rgb(50, 129, 168)
    private val colorByLabel = mutableMapOf<String, FloatArray>()
    private val componentEditorView = ComponentEditorView(events, assets)

    fun draw(pos: ImVec2, size: ImVec2, data: SceneEditor.ViewData) {
        ImGui.showDemoWindow()
        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Inspector##inspector")

        drawSceneProperties(data)

        ImGui.beginDisabled(data.selectedEntity == null)

        val entity = data.selectedEntity
        if (entity != null) {
            drawMainProperties(entity, data)

            val entityData = checkNotNull(data.selectedEntityData) {
                "Expected EntityData for entity $entity"
            }
            drawTemplateSection(entity, entityData)
        }

        ImGui.endDisabled()
        ImGui.end()
    }

    private fun drawSceneProperties(data: SceneEditor.ViewData) {
        ImGui.separatorText("Scene Properties")

        if (ImGui.treeNodeEx("Ambient Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            val label = "##sceneColorEdit"

            val color = data.ambientLight
            val selectedColor = colorByLabel.getOrPut(label, { floatArrayOf(color.r, color.g, color.b, color.a) })
            val refColor = floatArrayOf(color.r, color.g, color.b, color.a)
            ImGui.colorPicker4(label, selectedColor, refColor)

            if (ImGui.button("Apply")) {
                val newColor = Color(selectedColor[0], selectedColor[1], selectedColor[2], selectedColor[3])
                events.fire(SceneEditor.AmbientLightUpdated(newColor))
            }

            ImGui.treePop()
        }
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

    private fun drawTemplateSection(entity: Entity, data: EntityData) {
        ImGui.separatorText("")

        ImGui.inputText(
            /* label = */ "Template##inspectorTemplateName",
            /* text = */ ImString(data.template.name),
            /* imGuiInputTextFlags = */ ImGuiInputTextFlags.ReadOnly
        )

        for (templateComponent in data.template.components) {
            val componentOverride = data
                .componentOverrides
                .firstOrNull { c -> c::class == templateComponent::class }

            if (componentOverride == null) {
                drawTemplateComponent(entity, templateComponent)
            } else {
                drawComponentOverride(entity, data.template, componentOverride)
            }
        }
    }

    private fun drawTemplateComponent(entity: Entity, component: Component) {
        if (ImGui.checkbox("##inspectorComponentOverride-${component::class.simpleName}", false)) {
            events.fire(SceneEditor.ComponentOverrideEnabled(entity, component))
        }
        ImGui.setItemTooltip("Override Component")
        ImGui.sameLine()

        if (ImGui.collapsingHeader(component::class.simpleName, ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.beginDisabled()
            ImGui.indent()
            componentEditorView.draw(component)
            ImGui.unindent()
            ImGui.endDisabled()
        }
    }

    private fun drawComponentOverride(entity: Entity, template: EntityTemplate, component: Component) {
        if (ImGui.checkbox("##inspectorComponentOverride-${component::class.simpleName}", true)) {
            events.fire(SceneEditor.ComponentOverrideDisabled(entity, template, component))
        }
        ImGui.setItemTooltip("Override Component")
        ImGui.sameLine()

        ImGui.pushStyleColor(ImGuiCol.Header, componentOverrideColor)
        val open = ImGui.collapsingHeader(component::class.simpleName, ImGuiTreeNodeFlags.DefaultOpen)
        ImGui.popStyleColor()

        if (open) {
            ImGui.indent()
            componentEditorView.draw(component)
            ImGui.unindent()
        }
    }
}
