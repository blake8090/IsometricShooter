package bke.iso.editor2

import bke.iso.engine.Engine
import bke.iso.engine.beginImGui
import bke.iso.engine.core.Module
import bke.iso.engine.endImGui
import bke.iso.engine.input.ButtonState
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import com.badlogic.gdx.Input
import imgui.ImGui
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorImGuiState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = LinkedHashSet()
    override val modules: Set<Module> = emptySet()

    private val log = KotlinLogging.logger {}

    override suspend fun load() {
        log.info { "Starting editor" }

        engine.ui2.stop()
        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)
    }

    override fun onFrameEnd() {
        super.onFrameEnd()

        beginImGui()
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("Create")
                ImGui.menuItem("Open", "Ctrl+O")
                ImGui.menuItem("Save", "Ctrl+S")
                ImGui.menuItem("Save as..")
                ImGui.endMenu()
            }
            ImGui.endMainMenuBar()
        }

        ImGui.showDemoWindow()
        endImGui()
    }
}
