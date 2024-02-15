package bke.iso.engine.render.debug

import bke.iso.engine.render.shape.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.OrderedMap

class DebugRenderer {

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

    private fun enable() {
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
            enabledCategories.add(name)
        }
    }

    fun category(name: String): DebugCategory {
        if (!categories.containsKey(name)) {
            categories.put(name, DebugCategory())
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
