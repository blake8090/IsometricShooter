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
            categories[name]?.enabled = true
        }
    }

    private fun disable() {
        enabled = false
        for (category in categories.values()) {
            category.enabled = false
        }
    }

    fun enableCategories(vararg names: String) {
        for (name in names) {
            if (!enabledCategories.contains(name, false)) {
                enabledCategories.add(name)
            }
            if (enabled) {
                category(name).enabled = true
            }
        }
    }

    fun category(name: String): DebugCategory {
        if (!categories.containsKey(name)) {
            val category = DebugCategory(collisionBoxes)
            category.enabled = enabled && enabledCategories.contains(name, false)
            categories.put(name, category)
        }
        return categories.get(name)
    }

    fun draw(shapeRenderer: ShapeRenderer) {
        if (enabled) {
            for (name in enabledCategories) {
                drawCategory(name, shapeRenderer)
            }
        }

        for (category in categories.values()) {
            category.clear()
        }
    }

    private fun drawCategory(name: String, shapeRenderer: ShapeRenderer) {
        val category = categories[name] ?: return
        shapeRenderer.draw(category.shapes)
    }
}
