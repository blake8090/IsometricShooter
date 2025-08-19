package bke.iso.editor.entity

import bke.iso.editor.EditorModule
import bke.iso.editor.component.ComponentEditorView
import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.ui.imgui.ImGuiView
import bke.iso.engine.world.entity.Component
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags

class EntityEditorView(
    private val events: Events,
    assets: Assets,
) : ImGuiView() {

    private val componentEditorView = ComponentEditorView(events, assets)
    private var showDemoWindow = false

    private var openSelectComponentPopup = false

    lateinit var viewData: EntityEditor.ViewData

    override fun create() {}

    override fun handleEvent(event: Event) {}

    override fun drawImGui() {
        drawMainMenuBar()
        drawComponentEditor(viewData)
        drawSelectComponentPopup(viewData)

        if (showDemoWindow) {
            ImGui.showDemoWindow()
        }
    }

    private fun drawMainMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    events.fire(EntityEditor.OpenClicked())
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    events.fire(EntityEditor.SaveClicked())
                }

                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Mode")) {
                if (ImGui.menuItem("Scene Editor", false)) {
                    events.fire(EditorModule.SceneEditorSelected())
                }
                ImGui.menuItem("Entity Editor", true)
                ImGui.beginDisabled()
                ImGui.menuItem("Particle Editor", false)
                ImGui.menuItem("Animation Editor", false)
                ImGui.menuItem("Weapon Editor", false)
                ImGui.endDisabled()
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Debug")) {
                if (ImGui.menuItem("Show Demo Window", showDemoWindow)) {
                    showDemoWindow = !showDemoWindow
                }
                ImGui.endMenu()
            }

            ImGui.endMainMenuBar()
        }
    }

    private fun drawSelectComponentPopup(viewData: EntityEditor.ViewData) {
        if (openSelectComponentPopup) {
            ImGui.openPopup("Component Types##selectComponentPopup")
            openSelectComponentPopup = false
        }

        if (ImGui.beginPopupModal("Component Types##selectComponentPopup", null, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Select a component type to add.")
            if (ImGui.beginListBox("##components")) {
                for (componentType in viewData.componentTypes.sortedBy { type -> type.simpleName }) {
                    if (ImGui.selectable("${componentType.simpleName}", false)) {
                        events.fire(EntityEditor.NewComponentTypeAdded(componentType))
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endListBox()
            }

            if (ImGui.button("Cancel")) {
                ImGui.closeCurrentPopup()
            }

            ImGui.endPopup()
        }
    }

    private fun drawComponentEditor(viewData: EntityEditor.ViewData) {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)
        pos.x = ImGui.getMainViewport().workSizeX - size.x

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Component Editor")

        if (ImGui.button("Add")) {
            openSelectComponentPopup = true
        }

        val deletedComponents = mutableListOf<Component>()
        for (component in viewData.components) {
            if (ImGui.button("X##$component")) {
                deletedComponents.add(component)
            }

            ImGui.sameLine()
            if (ImGui.collapsingHeader(component::class.simpleName, ImGuiTreeNodeFlags.DefaultOpen)) {
                ImGui.indent()
                componentEditorView.draw(component)
                ImGui.unindent()
            }
        }

        for (component in deletedComponents) {
            events.fire(EntityEditor.ComponentDeleted(component))
        }

        ImGui.end()
    }
}
