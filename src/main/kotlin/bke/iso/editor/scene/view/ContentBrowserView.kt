package bke.iso.editor.scene.view

import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.BASE_PATH
import bke.iso.engine.core.Events
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.graphics.GLTexture
import com.badlogic.gdx.graphics.Texture
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileFilter

class ContentBrowserView(
    private val assets: Assets,
    private val events: Events
) {

    private val log = KotlinLogging.logger {}

    fun draw(pos: ImVec2, size: ImVec2, data: SceneEditor.ViewData) {
        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Content Browser##content")

        if (ImGui.beginTabBar("contentBrowserEntities")) {
            if (ImGui.beginTabItem("Entities")) {
                ImGui.beginChild(
                    /* strId = */ "##contentEntityBrowser",
                    /* size = */ ImVec2(size.x - 12f, size.y * 0.60f),
                    /* imGuiWindowFlags = */ ImGuiWindowFlags.HorizontalScrollbar
                )
                drawEntityAssets(data)
                ImGui.endChild()
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }

        if (ImGui.beginTabBar("contentBrowserFilesystem")) {
            if (ImGui.beginTabItem("Filesystem")) {
                ImGui.beginChild(
                    /* strId = */ "##contentFileBrowser",
                    /* size = */ ImVec2(size.x - 12f, size.y * 0.30f),
                    /* imGuiWindowFlags = */ ImGuiWindowFlags.HorizontalScrollbar
                )

                drawDirectory(File(BASE_PATH), data.selectedAssetDirectory)
                ImGui.endChild()
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }

        ImGui.end()
    }

    private fun drawDirectory(dir: File, selectedDir: File?) {
        val flags =
            or(
                ImGuiTreeNodeFlags.OpenOnArrow,
                ImGuiTreeNodeFlags.OpenOnDoubleClick,
                ImGuiTreeNodeFlags.SpanAvailWidth,
                if (dir == selectedDir) {
                    ImGuiTreeNodeFlags.Selected
                } else {
                    0
                }
            )

        val open = ImGui.treeNodeEx(dir.name, flags)
        if (ImGui.isItemClicked() && !ImGui.isItemToggledOpen()) {
            log.debug { "selected ${dir.path}" }
            events.fire(SceneEditor.AssetDirectorySelected(dir))
        }

        if (open) {
            for (subDir in getSubDirectories(dir)) {
                drawDirectory(subDir, selectedDir)
            }
            ImGui.treePop()
        }
    }

    private fun getSubDirectories(file: File): List<File> {
        val dirs = file.listFiles(FileFilter(File::isDirectory)) ?: emptyArray()
        return dirs.toList()
    }

    private fun drawEntityAssets(data: SceneEditor.ViewData) {
        var column = 0

        for (template in data.entityTemplatesInDirectory) {
            val sprite = template
                .components
                .firstNotNullOfOrNull { component -> component as? Sprite }
                ?: continue

            if (imageButton(template.name, sprite.texture)) {
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

    private fun imageButton(templateName: String, texture: String): Boolean {
        val tex = assets.get<Texture>(texture)
        val texId = (tex as GLTexture).textureObjectHandle
        return ImGui.imageButton(
            /* strId = */ "$templateName-$texture",
            /* userTextureId = */ texId.toLong(),
            /* size = */ ImVec2(tex.width.toFloat(), tex.height.toFloat())
        )
    }
}
