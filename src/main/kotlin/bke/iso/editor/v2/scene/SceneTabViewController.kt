package bke.iso.editor.v2.scene

import bke.iso.editor.v2.core.EditorViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.core.Module
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneTabViewController(
    skin: Skin,
    private val assets: Assets,
) : EditorViewController<SceneTabView>() {

    private val log = KotlinLogging.logger { }

    override val modules: Set<Module> = emptySet()
    override val view: SceneTabView = SceneTabView(skin, assets)

    override fun start() {
        log.debug { "Starting SceneTabViewController" }
        val assetList = mutableListOf<Any>()
        assetList.addAll(assets.getAll<TilePrefab>())
        assetList.addAll(assets.getAll<ActorPrefab>())
        view.updateAssetBrowser(assetList)
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
    }
}
