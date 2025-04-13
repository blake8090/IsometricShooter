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

        var menuBarHeight = 0f
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")
                ImGui.menuItem("Open", "Ctrl+O")
                ImGui.menuItem("Save", "Ctrl+S")
                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }
            menuBarHeight = ImGui.getFrameHeight()
            ImGui.endMainMenuBar()
        }

        drawAssetBrowser(menuBarHeight)
        drawInspectorWindow(menuBarHeight)
        ImGui.showDemoWindow()
        endImGuiFrame()
    }

    private fun drawAssetBrowser(posY: Float) {
        val pos = ImVec2(0f, posY)

        val size = ImVec2(viewport.size)
        size.x *= 0.25f
        size.y *= 0.5f

        ImGui.setNextWindowPos(pos)
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

    private fun imageButton(texture: String): Boolean {
        val tex = assets.get<Texture>(texture)
        val texId = (tex as GLTexture).textureObjectHandle
        return ImGui.imageButton(
            /* strId = */ texture,
            /* userTextureId = */ texId.toLong(),
            /* size = */ ImVec2(tex.width.toFloat(), tex.height.toFloat())
        )
    }

    private fun drawInspectorWindow(posY: Float) {
        val viewport = ImGui.getMainViewport()

        val size = ImVec2(viewport.size)
        size.x *= 0.2f
        size.y *= 0.5f

        ImGui.setNextWindowPos(ImVec2(viewport.sizeX - size.x, posY))
        ImGui.setNextWindowSize(size)
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
}
