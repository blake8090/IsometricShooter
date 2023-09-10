package bke.iso.engine.world

import com.fasterxml.jackson.annotation.JsonTypeInfo

// TODO: interface or nah?
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
open class Component

annotation class ComponentSubType(val name: String)
