package bke.iso.editor2.actor

import bke.iso.editor2.ImGuiEditorState
import bke.iso.editor2.scene.SceneMode
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.core.Events
import bke.iso.engine.endImGuiFrame
import imgui.ImGui
import imgui.ImVec2

class ActorModeView(private val events: Events) {

    fun draw() {
        beginImGuiFrame()
        drawMainMenuBar()
        endImGuiFrame()
    }

    private fun drawMainMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    events.fire(SceneMode.OpenSceneClicked())
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    events.fire(SceneMode.SaveSceneClicked())
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
        ImGui.showDemoWindow()

        drawComponentList()
    }

    private fun drawComponentList() {
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
            ImGui.endListBox()
        }

        ImGui.end()
    }
}
