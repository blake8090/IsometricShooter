package bke.iso.editor2.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Texture
import imgui.ImGui
import imgui.ImVec2
import imgui.type.ImFloat
import imgui.type.ImString
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneModeView(private val assets: Assets) {

    private val log = KotlinLogging.logger { }

    private val viewport = ImGui.getMainViewport()

    fun draw() {
        beginImGuiFrame()

        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")
                ImGui.menuItem("Open", "Ctrl+O")
                ImGui.menuItem("Save", "Ctrl+S")
                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }
            ImGui.endMainMenuBar()
        }

        val assetBrowserWindowData = getAssetBrowserWindowData()
        val toolbarWindowData = getToolbarWindowData(assetBrowserWindowData)
        val inspectorWindowData = getInspectorWindowData(assetBrowserWindowData, toolbarWindowData)
        drawAssetBrowser(assetBrowserWindowData)
        drawToolbar(toolbarWindowData)
        drawInspectorWindow(inspectorWindowData)

        endImGuiFrame()
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

    private fun imageButton(texture: String, scale: Float = 1f): Boolean {
        val tex = assets.get<Texture>(texture)
        val texId = (tex as GLTexture).textureObjectHandle
        return ImGui.imageButton(
            /* strId = */ texture,
            /* userTextureId = */ texId.toLong(),
            /* size = */ ImVec2(tex.width.toFloat() * scale, tex.height.toFloat() * scale)
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

    private fun drawToolbar(windowData: WindowData) {
        ImGui.setNextWindowPos(windowData.pos)
        ImGui.setNextWindowSize(windowData.size)
        ImGui.begin("Toolbar")

        imageButton("pointer.png")
        ImGui.sameLine()
        imageButton("brush.png")
        ImGui.sameLine()
        imageButton("eraser.png")
        ImGui.sameLine()
        imageButton("icon-room.png")
        ImGui.sameLine()
        imageButton("fill.png")
        ImGui.sameLine()
        imageButton("grid.png")
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
