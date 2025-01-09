package bke.iso.editor.v2.actor

import bke.iso.editor.EditorEvent
import bke.iso.editor.v2.EditorViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.core.Module
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class ActorTabViewController(skin: Skin, assets: Assets) : EditorViewController<ActorTabView>() {

    override val modules: Set<Module> = emptySet()

    override val view: ActorTabView = ActorTabView(skin, assets)

    override fun handleEditorEvent(event: EditorEvent) {
    }
}
