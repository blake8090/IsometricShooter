package bke.iso.engine.ui.v3.imgui

import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.ui.v3.UIView

abstract class ImGuiView : UIView() {

    override fun draw(deltaTime: Float) {
        beginImGuiFrame()
        drawImGui()
        endImGuiFrame()
    }

    abstract fun drawImGui()
}
