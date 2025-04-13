package bke.iso.editor2

import bke.iso.engine.Engine
import bke.iso.engine.beginImGui
import bke.iso.engine.core.Module
import bke.iso.engine.endImGui
import bke.iso.engine.input.ButtonState
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import imgui.ImGui
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorImGuiState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = LinkedHashSet()
    override val modules: Set<Module> = emptySet()

    private val log = KotlinLogging.logger {}

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    var selectedLayer = 0f
        private set

    override suspend fun load() {
        log.info { "Starting editor" }

        engine.ui2.stop()
        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        drawGrid()
    }

    private fun drawGrid() {
        val shapes = getShapesArray()

        for (x in 0..gridWidth) {
            shapes.addLine(
                Vector3(x.toFloat(), 0f, selectedLayer),
                Vector3(x.toFloat(), gridLength.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
        for (y in 0..gridLength) {
            shapes.addLine(
                Vector3(0f, y.toFloat(), selectedLayer),
                Vector3(gridWidth.toFloat(), y.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
    }

    private fun getShapesArray() =
        if (drawGridForeground) {
            engine.renderer.fgShapes
        } else {
            engine.renderer.bgShapes
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
