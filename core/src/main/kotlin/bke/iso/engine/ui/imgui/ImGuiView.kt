package bke.iso.engine.ui.imgui

import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.ui.UIView

abstract class ImGuiView : UIView() {

    override fun draw(deltaTime: Float) {
        beginImGuiFrame()
        drawImGui()
        endImGuiFrame()
    }

    abstract fun drawImGui()
}
