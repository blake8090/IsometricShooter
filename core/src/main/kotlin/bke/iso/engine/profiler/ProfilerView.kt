package bke.iso.engine.profiler

import bke.iso.engine.core.Event
import bke.iso.engine.ui.imgui.ImGuiView
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags

class ProfilerView : ImGuiView() {
    private var text: String = ""

    override fun create() {}

    override fun handleEvent(event: Event) {}

    override fun drawImGui() {
        // TODO: use or helper function
        val flags = ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.NoResize or
                ImGuiWindowFlags.NoCollapse or
                ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.AlwaysAutoResize

        ImGui.setNextWindowPos(10f, 10f)
        ImGui.begin("Profiler", flags)
        ImGui.text(text)
        ImGui.end()
    }

    fun setText(text: String) {
        this.text = text
    }
}