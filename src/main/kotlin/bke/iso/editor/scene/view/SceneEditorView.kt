package bke.iso.editor.scene.view

import bke.iso.editor.EditorModule
import bke.iso.editor.scene.SceneEditor
import bke.iso.editor.scene.tool.ToolSelection
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.imgui.ImGuiView
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Texture
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImString
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneEditorView(
    private val assets: Assets,
    private val events: Events
) : ImGuiView() {

    private val log = KotlinLogging.logger { }

    private val viewport = ImGui.getMainViewport()

    private var openSelectBuildingPopup = false
    private var selectBuildingPopupAction: (String) -> Unit = {}
    private var openNewBuildingPopup = false
    private var newBuildingText = ""

    private val inspectorWindowView = InspectorWindowView(assets, events)

    lateinit var viewData: SceneEditor.ViewData

    override fun create() {}

    override fun handleEvent(event: Event) {}

    override fun drawImGui() {
        if (!this::viewData.isInitialized) {
            return
        }
        drawMainMenuBar(viewData)
        drawSelectBuildingPopup(viewData)
        drawNewBuildingPopup()

        val assetBrowserWindowData = getAssetBrowserWindowData()
        val toolbarWindowData = getToolbarWindowData(assetBrowserWindowData)
        val inspectorWindowData = getInspectorWindowData(assetBrowserWindowData, toolbarWindowData)
        drawAssetBrowser(assetBrowserWindowData)
        drawToolbar(toolbarWindowData, viewData)
        drawMessageWindow(toolbarWindowData, viewData)
        inspectorWindowView.draw(inspectorWindowData.pos, inspectorWindowData.size, viewData)
//        drawInspectorWindow(inspectorWindowData, viewData)
    }

    private fun drawMainMenuBar(viewData: SceneEditor.ViewData) {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    events.fire(SceneEditor.OpenSceneClicked())
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                    events.fire(SceneEditor.SaveSceneClicked())
                }

                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("View")) {
//                if (ImGui.menuItem("Show Grid", viewData.showGrid)) {

//                }
//                if (ImGui.menuItem("Show Collision", false)) {

//                }
                if (ImGui.menuItem("Hide Walls", viewData.hideWalls)) {
                    events.fire(SceneEditor.HideWallsToggled())
                }
                if (ImGui.menuItem("Hide Upper Layers", viewData.hideUpperLayers)) {
                    events.fire(SceneEditor.HideUpperLayersToggled())
                }
                if (ImGui.menuItem("Highlight Selected Layer", viewData.highlightSelectedLayer)) {
                    events.fire(SceneEditor.HighlightSelectedLayerToggled())
                }
//                if (ImGui.menuItem("Show Grid in Foreground", false)) {
//
//                }
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Buildings")) {
                if (ImGui.menuItem("New Building", false)) {
                    openNewBuildingPopup()
                }
                if (ImGui.menuItem("Edit Building", false)) {
                    openSelectBuildingPopup { building ->
                        events.fire(SceneEditor.BuildingSelected(building))
                    }
                }

                ImGui.beginDisabled(viewData.selectedBuilding.isNullOrBlank())
                if (ImGui.menuItem("Close Building", false)) {
                    events.fire(SceneEditor.BuildingClosed())
                }
                ImGui.endDisabled()

                if (ImGui.menuItem("Delete Building", false)) {
                    openSelectBuildingPopup { building ->
                        events.fire(SceneEditor.BuildingDeleted(building))
                    }
                }
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Mode")) {
                ImGui.menuItem("Scene Editor", true)
                if (ImGui.menuItem("Entity Editor", false)) {
                    events.fire(EditorModule.EntityEditorSelected())
                }
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

    private fun drawAssetBrowser(windowData: WindowData) {
        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        ImGui.begin("Asset Browser")

        if (ImGui.beginTabBar("assetTypes")) {
            if (ImGui.beginTabItem("Entities")) {
                drawEntityAssets()
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

    private fun drawEntityAssets() {
        var column = 0
        for (template in assets.getAll<EntityTemplate>()) {
            val sprite = template
                .components
                .firstNotNullOfOrNull { component -> component as? Sprite }
                ?: continue

            if (imageButton(sprite.texture)) {
                log.debug { "Selected entity template ${template.name}" }
                events.fire(SceneEditor.EntityTemplateSelected(template))
            }
            ImGui.setItemTooltip(template.name)

            if (column < 2) {
                ImGui.sameLine()
                column++
            } else {
                column = 0
            }
        }
    }

    private fun drawTileAssets() {
//        var column = 0
//
//        val tiles = assets.getAll<EntityTemplate>()
//            .filter { entityTemplate -> entityTemplate.components }
//        for (template in assets.getAll<TileTemplate>()) {
//            if (imageButton(template.sprite.texture)) {
//                log.debug { "Selected tile template ${template.name}" }
//                events.fire(SceneEditor.TileTemplateSelected(template))
//            }
//            ImGui.setItemTooltip(template.name)
//
//            if (column < 2) {
//                ImGui.sameLine()
//                column++
//            } else {
//                column = 0
//            }
//        }
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

    private fun drawToolbar(windowData: WindowData, viewData: SceneEditor.ViewData) {
        val selectedTool = viewData.selectedTool

        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        val flags = or(
            ImGuiWindowFlags.NoMove,
            ImGuiWindowFlags.NoResize,
            ImGuiWindowFlags.NoTitleBar,
            ImGuiWindowFlags.NoScrollbar
        )
        ImGui.begin("Toolbar", flags)

        toolbarImageButton("pointer.png", selectedTool == ToolSelection.POINTER) {
            events.fire(SceneEditor.ToolSelected(ToolSelection.POINTER))
        }
        ImGui.sameLine()
        toolbarImageButton("brush.png", selectedTool == ToolSelection.BRUSH) {
            events.fire(SceneEditor.ToolSelected(ToolSelection.BRUSH))
        }
        ImGui.sameLine()
        toolbarImageButton("eraser.png", selectedTool == ToolSelection.ERASER) {
            events.fire(SceneEditor.ToolSelected(ToolSelection.ERASER))
        }
        ImGui.sameLine()
        toolbarImageButton("icon-room.png", selectedTool == ToolSelection.ROOM) {
            events.fire(SceneEditor.ToolSelected(ToolSelection.ROOM))
        }
        ImGui.sameLine()
        toolbarImageButton("fill.png", selectedTool == ToolSelection.FILL) {
            events.fire(SceneEditor.ToolSelected(ToolSelection.FILL))
        }
        ImGui.sameLine()
        toolbarImageButton("grid.png")
        ImGui.sameLine()

        ImGui.text("Layer: ${viewData.selectedLayer}")
        ImGui.sameLine()
        if (ImGui.button("-")) {
            events.fire(SceneEditor.LayerDecreased())
        }
        ImGui.sameLine()
        if (ImGui.button("+")) {
            events.fire(SceneEditor.LayerIncreased())
        }
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

    private fun drawMessageWindow(windowData: WindowData, viewData: SceneEditor.ViewData) {
        val pos = ImVec2(windowData.pos)
        pos.y += windowData.size.y

        val size = ImVec2(windowData.size)
        size.y = ImGui.getTextLineHeightWithSpacing()

        ImGui.getTextLineHeight()
        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)

        val flags = or(
            ImGuiWindowFlags.NoMove,
            ImGuiWindowFlags.NoResize,
            ImGuiWindowFlags.NoTitleBar,
            ImGuiWindowFlags.NoScrollbar
        )
        ImGui.begin("MessageWindow", flags)

        ImGui.text(viewData.messageBarText)

        ImGui.end()
    }

    private fun getAssetBrowserWindowData(): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 0.15f

        val pos = ImVec2(viewport.workPos)

        return WindowData(pos, size)
    }

    private fun getToolbarWindowData(assetBrowserData: WindowData): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 1f - 0.15f - 0.2f
        size.y = 95f

        val pos = ImVec2(viewport.workPos)
        pos.x += assetBrowserData.size.x

        return WindowData(pos, size)
    }

    private fun getInspectorWindowData(assetBrowserData: WindowData, toolbarWindowData: WindowData): WindowData {
        val size = ImVec2(viewport.workSize)
        size.x *= 0.2f

        val pos = ImVec2(viewport.workPos)
        pos.x += assetBrowserData.size.x + toolbarWindowData.size.x

        return WindowData(pos, size)
    }

    private fun openSelectBuildingPopup(action: (String) -> Unit) {
        openSelectBuildingPopup = true
        selectBuildingPopupAction = action
    }

    private fun drawSelectBuildingPopup(viewData: SceneEditor.ViewData) {
        if (openSelectBuildingPopup) {
            ImGui.openPopup("Select Building##selectBuildingPopup")
        }

        if (ImGui.beginPopupModal("Select Building##selectBuildingPopup", null, ImGuiWindowFlags.AlwaysAutoResize)) {
            if (ImGui.beginListBox("##buildings")) {
                for (building in viewData.buildings) {
                    if (ImGui.selectable(building, false)) {
                        selectBuildingPopupAction.invoke(building)
                        openSelectBuildingPopup = false
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endListBox()
            }

            if (ImGui.button("Cancel")) {
                openSelectBuildingPopup = false
                ImGui.closeCurrentPopup()
            }

            ImGui.endPopup()
        }
    }

    private fun openNewBuildingPopup() {
        openNewBuildingPopup = true
        newBuildingText = ""
    }

    private fun drawNewBuildingPopup() {
        if (openNewBuildingPopup) {
            ImGui.openPopup("New Building##newBuildingPopup")
        }

        if (ImGui.beginPopupModal("New Building##newBuildingPopup", null, ImGuiWindowFlags.AlwaysAutoResize)) {
            val text = ImString(newBuildingText, 50)
            if (ImGui.inputText("Name", text)) {
                newBuildingText = text.get()
            }

            if (ImGui.button("Ok")) {
                events.fire(SceneEditor.BuildingCreated(newBuildingText))
                openNewBuildingPopup = false
                ImGui.closeCurrentPopup()
            }
            ImGui.sameLine()
            if (ImGui.button("Cancel")) {
                openNewBuildingPopup = false
                ImGui.closeCurrentPopup()
            }

            ImGui.endPopup()
        }
    }

    private data class WindowData(
        val pos: ImVec2,
        val size: ImVec2,
    )
}

private fun or(vararg integers: Int): Int {
    var result = 0
    integers.forEach { integer ->
        result = result or integer
    }
    return result
}
