package bke.iso.editor2.actor

import bke.iso.editor2.ImGuiEditorState
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.core.Events
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.world.actor.Component
import imgui.ImGui
import imgui.ImVec2

class ActorModeView(private val events: Events) {

    private var selectedIndex = -1

    fun reset() {
        selectedIndex = -1
    }

    fun draw(components: List<Component>) {
        beginImGuiFrame()
        drawMainMenuBar()
        drawComponentList(components)
        endImGuiFrame()
    }

    private fun drawMainMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    events.fire(ActorMode.OpenClicked())
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                }

                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Mode")) {
                if (ImGui.menuItem("Scene Editor", false)) {
                    events.fire(ImGuiEditorState.SceneModeSelected())
                }
                ImGui.menuItem("Actor Editor", true)
                ImGui.beginDisabled()
                ImGui.menuItem("Particle Editor", false)
                ImGui.menuItem("Animation Editor", false)
                ImGui.menuItem("Weapon Editor", false)
                ImGui.endDisabled()
                ImGui.endMenu()
            }

            ImGui.endMainMenuBar()
        }
    }

    private fun drawComponentList(components: List<Component>) {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Components")

        ImGui.button("Add")
        ImGui.sameLine()
        ImGui.button("Delete")

        if (ImGui.beginListBox("##components", size.x, 5 * ImGui.getTextLineHeightWithSpacing())) {
            for ((index, component) in components.withIndex()) {
                val selected = selectedIndex == index
                if (ImGui.selectable("${component::class.simpleName}", selected)) {
                    selectedIndex = index
                }
            }

            ImGui.endListBox()
        }

        ImGui.end()
    }
}
