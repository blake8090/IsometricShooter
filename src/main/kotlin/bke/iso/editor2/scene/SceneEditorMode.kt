package bke.iso.editor2.scene

import bke.iso.editor2.EditorMode
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Texture
import imgui.ImGui
import imgui.ImVec2
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneEditorMode(
    renderer: Renderer,
    world: World,
    private val assets: Assets,
) : EditorMode(renderer, world) {

    private val log = KotlinLogging.logger { }

    override fun update() {
    }

    override fun draw() {
        beginImGuiFrame()

        // TODO: move all this to SceneEditorView
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
        endImGuiFrame()
    }

    private fun drawAssetBrowser(height: Float) {
        val viewport = ImGui.getMainViewport()

        // draw this just under the main menu bar!
        val pos = ImVec2(viewport.pos)
        pos.x = 0f
        pos.y = height

        val size = ImVec2(viewport.size)
        size.x *= 0.25f
        size.y *= 0.5f

        ImGui.setNextWindowPos(pos)
        ImGui.begin("Asset Browser")

        if (ImGui.beginTabBar("assetTypes")) {
            if (ImGui.beginTabItem("Actors")) {
                ImGui.text("All actor assets here")
                drawActorAssets()
                ImGui.endTabItem()
            }

            if (ImGui.beginTabItem("Tiles")) {
                ImGui.text("All tile assets here")
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

            ImGui.beginGroup()
            imageButton(sprite.texture)
            ImGui.setItemTooltip(prefab.name)
            ImGui.endGroup()

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
            ImGui.beginGroup()
            imageButton(prefab.sprite.texture)
            ImGui.setItemTooltip(prefab.name)
            ImGui.endGroup()

            if (column < 2) {
                ImGui.sameLine()
                column++
            } else {
                column = 0
            }
        }
    }

    private fun imageButton(texture: String) {
        val tex = assets.get<Texture>(texture)
        val texId = (tex as GLTexture).textureObjectHandle
        ImGui.imageButton(
            /* strId = */ texture,
            /* userTextureId = */ texId.toLong(),
            /* size = */ ImVec2(tex.width.toFloat(), tex.height.toFloat())
        )
    }
}
