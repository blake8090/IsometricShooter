package bke.iso.engine.world

import com.fasterxml.jackson.annotation.JsonTypeInfo

// TODO: interface or nah?
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
open class Component

// TODO: cant we use JsonSubType instead?
annotation class ComponentSubType(val name: String)
