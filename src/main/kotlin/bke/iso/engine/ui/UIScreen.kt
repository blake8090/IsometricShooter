package bke.iso.engine.ui

import bke.iso.engine.Disposer
import bke.iso.engine.Event
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.reflect.KClass

abstract class UIScreen {

    val stage: Stage = Stage(ScreenViewport())
    val controllerNavigation: ControllerNavigation = ControllerNavigation()

    protected val skin: Skin = DisposerSkin()

    abstract fun create()

    fun render(deltaTime: Float) {
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

private class DisposerSkin : Skin() {

    private val types = mutableSetOf<KClass<out Disposable>>()

    override fun add(name: String, resource: Any, type: Class<*>) {
        super.add(name, resource, type)
        val disposable = resource as? Disposable ?: return
        types.add(disposable::class)
    }

    override fun dispose() {
        val entries = types.flatMap { type -> getAll(type.java) ?: emptyList() }
        for (entry in entries) {
            Disposer.dispose(entry.value, entry.key)
        }
    }
}
