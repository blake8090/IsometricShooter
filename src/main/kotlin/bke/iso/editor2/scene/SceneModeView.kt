package bke.iso.editor2.scene

import bke.iso.editor2.ImGuiEditorState
import bke.iso.editor2.scene.tool.ToolSelection
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.core.Events
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Texture
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.type.ImFloat
import imgui.type.ImString
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneModeView(
    private val assets: Assets,
    private val events: Events
) {

    private val log = KotlinLogging.logger { }

    private val viewport = ImGui.getMainViewport()

    fun draw(selectedTool: ToolSelection) {
        beginImGuiFrame()

        drawMainMenuBar()

        val assetBrowserWindowData = getAssetBrowserWindowData()
        val toolbarWindowData = getToolbarWindowData(assetBrowserWindowData)
        val inspectorWindowData = getInspectorWindowData(assetBrowserWindowData, toolbarWindowData)
        drawAssetBrowser(assetBrowserWindowData)
        drawToolbar(toolbarWindowData, selectedTool)
        drawInspectorWindow(inspectorWindowData)

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

            if (ImGui.beginMenu("View")) {
                if (ImGui.menuItem("Show Grid", false)) {

                }
                if (ImGui.menuItem("Show Collision", false)) {

                }
                if (ImGui.menuItem("Hide Walls", false)) {

                }
                if (ImGui.menuItem("Hide Upper Layers", false)) {

                }
                if (ImGui.menuItem("Highlight Selected Layer", false)) {

                }
                if (ImGui.menuItem("Show Grid in Foreground", false)) {

                }
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Buildings")) {
                if (ImGui.menuItem("New Building", false)) {

                }
                if (ImGui.menuItem("Edit Building", false)) {

                }
                ImGui.beginDisabled()
                if (ImGui.menuItem("Close Building", false)) {

                }

                if (ImGui.menuItem("Delete Building", false)) {

                }
                ImGui.endDisabled()
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Mode")) {
                ImGui.menuItem("Scene Editor", true)
                if (ImGui.menuItem("Actor Editor", false)) {
                    events.fire(ImGuiEditorState.ActorPrefabModeSelected())
                }
                ImGui.beginDisabled()
                ImGui.menuItem("Particle Editor", false)
                ImGui.menuItem("Animation Editor", false)
                ImGui.menuItem("Weapon Editor", false)
                ImGui.endDisabled()
                ImGui.endMenu()
            }

            ImGui.showDemoWindow()
            ImGui.endMainMenuBar()
        }
    }

    private fun drawAssetBrowser(windowData: WindowData) {
        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        ImGui.begin("Asset Browser")

        if (ImGui.beginTabBar("assetTypes")) {
            if (ImGui.beginTabItem("Actors")) {
                drawActorAssets()
                ImGui.endTabItem()
            }

            if (ImGui.beginTabItem("Tiles")) {
                drawTileAssets()
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }

        ImGui.end()
    }

    private fun drawActorAssets() {
        var column = 0
        for (prefab in assets.getAll<ActorPrefab>()) {
            val sprite = prefab
                .components
                .firstNotNullOfOrNull { component -> component as? Sprite }
                ?: continue

            if (imageButton(sprite.texture)) {
                log.debug { "Selected actor prefab ${prefab.name}" }
                events.fire(SceneMode.ActorPrefabSelected(prefab))
            }
            ImGui.setItemTooltip(prefab.name)

            if (column < 2) {
                ImGui.sameLine()
                column++
            } else {
                column = 0
            }
        }
    }

    private fun drawTileAssets() {
        var column = 0
        for (prefab in assets.getAll<TilePrefab>()) {
            if (imageButton(prefab.sprite.texture)) {
                log.debug { "Selected tile prefab ${prefab.name}" }
                events.fire(SceneMode.TilePrefabSelected(prefab))
            }
            ImGui.setItemTooltip(prefab.name)

            if (column < 2) {
                ImGui.sameLine()
                column++
            } else {
                column = 0
            }
        }
    }

    private fun imageButton(texture: String): Boolean {
        val tex = assets.get<Texture>(texture)
        val texId = (tex as GLTexture).textureObjectHandle
        return ImGui.imageButton(
            /* strId = */ texture,
            /* userTextureId = */ texId.toLong(),
            /* size = */ ImVec2(tex.width.toFloat(), tex.height.toFloat())
        )
    }

    private fun drawInspectorWindow(windowData: WindowData) {
        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        ImGui.begin("Inspector")

        ImGui.beginDisabled()
        ImGui.inputText("id", ImString())
        ImGui.inputText("description", ImString())

        ImGui.separatorText("Position")
        ImGui.inputFloat("x", ImFloat())
        ImGui.inputFloat("y", ImFloat())
        ImGui.inputFloat("z", ImFloat())

        ImGui.separatorText("Tags")
        ImGui.inputText("name", ImString())
        ImGui.sameLine()
        ImGui.button("Add")

        ImGui.separatorText("Buildings")
        ImGui.button("Apply")
        ImGui.endDisabled()

        ImGui.end()
    }

    private fun drawToolbar(windowData: WindowData, selectedTool: ToolSelection) {
        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        ImGui.begin("Toolbar")

        toolbarImageButton("pointer.png", selectedTool == ToolSelection.POINTER) {
            events.fire(SceneMode.ToolSelected(ToolSelection.POINTER))
        }
        ImGui.sameLine()
        toolbarImageButton("brush.png", selectedTool == ToolSelection.BRUSH) {
            events.fire(SceneMode.ToolSelected(ToolSelection.BRUSH))
        }
        ImGui.sameLine()
        toolbarImageButton("eraser.png", selectedTool == ToolSelection.ERASER) {
            events.fire(SceneMode.ToolSelected(ToolSelection.ERASER))
        }
        ImGui.sameLine()
        toolbarImageButton("icon-room.png", selectedTool == ToolSelection.ROOM) {
            events.fire(SceneMode.ToolSelected(ToolSelection.ROOM))
        }
        ImGui.sameLine()
        toolbarImageButton("fill.png", selectedTool == ToolSelection.FILL) {
            events.fire(SceneMode.ToolSelected(ToolSelection.FILL))
        }
        ImGui.sameLine()
        toolbarImageButton("grid.png")
        ImGui.sameLine()

        ImGui.text("Layer: 01")
        ImGui.sameLine()
        ImGui.button("-")
        ImGui.sameLine()
        ImGui.button("+")
        ImGui.sameLine()

        imageButton("hide-layers.png")
        ImGui.sameLine()
        imageButton("hide-walls.png")
        ImGui.sameLine()
        imageButton("highlight-layer.png")
        ImGui.sameLine()

        ImGui.end()
    }

    private fun toolbarImageButton(texture: String, selected: Boolean = false, action: () -> Unit = {}) {
        if (selected) {
            ImGui.pushStyleColor(ImGuiCol.Button, ImGui.getStyleColorVec4(ImGuiCol.ButtonHovered))
        }

        if (imageButton(texture)) {
            action.invoke()
        }

        if (selected) {
            ImGui.popStyleColor()
        }
    }

    private fun getAssetBrowserWindowData(): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 0.15f

        val pos = ImVec2(viewport.workPos)

        return WindowData(pos, size)
    }

    private fun getToolbarWindowData(assetBrowserData: WindowData): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 1f - 0.15f - 0.16f
        size.y = 95f

        val pos = ImVec2(viewport.workPos)
        pos.x += assetBrowserData.size.x

        return WindowData(pos, size)
    }

    private fun getInspectorWindowData(assetBrowserData: WindowData, toolbarWindowData: WindowData): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 0.16f

        val pos = ImVec2(viewport.workPos)
        pos.x += assetBrowserData.size.x + toolbarWindowData.size.x

        return WindowData(pos, size)
    }

    private data class WindowData(
        val pos: ImVec2,
        val size: ImVec2,
    )
}
