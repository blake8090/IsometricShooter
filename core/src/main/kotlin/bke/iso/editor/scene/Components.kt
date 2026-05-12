package bke.iso.editor.scene

import bke.iso.engine.world.entity.Component

data class EntityTemplateReference(
    val template: String = "",
    val componentOverrides: MutableSet<Component> = mutableSetOf()
) : Component
