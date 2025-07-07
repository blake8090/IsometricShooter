package bke.iso.engine

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import imgui.ImGui
import imgui.ImGuiIO
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw

private lateinit var imGuiGlfw: ImGuiImplGlfw
private lateinit var imGuiGl3: ImGuiImplGl3
private var tmpProcessor: InputProcessor? = null

fun initImGui() {
    imGuiGlfw = ImGuiImplGlfw()
    imGuiGl3 = ImGuiImplGl3()
    val windowHandle = (Gdx.graphics as Lwjgl3Graphics).window.windowHandle
    ImGui.createContext()

    val io: ImGuiIO = ImGui.getIO()
    io.iniFilename = null
    io.fonts.addFontFromFileTTF("assets/ui/roboto.ttf", 18f)
    io.fonts.build()

    imGuiGlfw.init(windowHandle, true)
    imGuiGl3.init("#version 150")
}

fun beginImGuiFrame() {
    if (tmpProcessor != null) { // Restore the input processor after ImGui caught all inputs, see #end()
        Gdx.input.inputProcessor = tmpProcessor
        tmpProcessor = null
    }

    imGuiGl3.newFrame()
    imGuiGlfw.newFrame()
    ImGui.newFrame()
}

fun endImGuiFrame() {
    ImGui.render()
    imGuiGl3.renderDrawData(ImGui.getDrawData())

    // If ImGui wants to capture the input, disable libGDX's input processor
    if (imGuiWantsToCaptureInput()) {
        tmpProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = null
    }
}

fun imGuiWantsToCaptureInput() =
    ImGui.getIO().wantCaptureKeyboard || ImGui.getIO().wantCaptureMouse