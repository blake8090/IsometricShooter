package bke.iso.engine.render.debug

import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.render.shape.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.OrderedMap

class DebugRenderer(private val collisionBoxes: CollisionBoxes) {

    private var enabled = false
    private val categories = OrderedMap<String, DebugCategory>()
    private val enabledCategories = Array<String>()

    fun toggle() {
        if (enabled) {
            disable()
        } else {
            enable()
        }
    }

    fun enable() {
        enabled = true

        for (name in enabledCategories) {
            category(name).enabled = true
        }
    }

    private fun disable() {
        enabled = false

        for (category in categories.values()) {
            category.enabled = false
        }
    }

    fun enableCategories(vararg names: String) {
        names.forEach(enabledCategories::add)
    }

    fun category(name: String): DebugCategory {
        if (!categories.containsKey(name)) {
            categories.put(name, DebugCategory(collisionBoxes))
        }
        return categories.get(name)
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        if (!enabled) {
            return
        }

        for (category in categories.values()) {
            if (category.enabled) {
                shapeRenderer.draw(category.shapes)
            }
            category.clear()
        }
    }
}
