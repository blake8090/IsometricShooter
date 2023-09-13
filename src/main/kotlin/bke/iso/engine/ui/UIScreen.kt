package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.ControllerNavigation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import mu.KotlinLogging

abstract class UIScreen(protected val assets: Assets) {

    val stage: Stage = Stage(ScreenViewport())
    val controllerNavigation: ControllerNavigation = ControllerNavigation()

    protected val skin: Skin = AssetAwareSkin(assets)

    abstract fun create()

    open fun draw(deltaTime: Float) {
        stage.act(deltaTime)
        stage.viewport.apply()
        stage.draw()
    }

    fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    fun dispose() {
        stage.dispose()
        skin.dispose()
    }

    open fun handleEvent(event: Event) {}
}

/**
 * Multiple UI screens can reference a single resource loaded in [Assets].
 * Different UI screens will continuously be created and disposed throughout the app's lifetime.
 *
 * The [AssetAwareSkin] manually keeps track of all added resources, as [Skin.resources] is private.
 * When [Skin.dispose] is called, any resources loaded in [Assets] are skipped.
 * Only resources local to this instance are disposed.
 */
private class AssetAwareSkin(private val assets: Assets) : Skin() {

    private val log = KotlinLogging.logger {}
    private val resources = mutableSetOf<Any>()

    override fun add(name: String, resource: Any, type: Class<*>) {
        super.add(name, resource, type)
        resources.add(resource)
    }

    override fun dispose() {
        if (atlas != null) {
            dispose(atlas)
        }
        for (resource in resources) {
            dispose(resource)
        }
    }

    private fun <T : Any> dispose(resource: T) {
        if (resource in assets || resource in assets.fonts) {
            log.debug { "Skipping '$resource' - resource is loaded in asset cache" }
        } else if (resource is Disposable) {
            resource.dispose()
        }
    }
}
