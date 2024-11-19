package bke.iso.engine.asset.config

import bke.iso.engine.asset.Assets
import kotlin.reflect.KClass
import kotlin.reflect.cast

class Configs(private val assets: Assets) {

    fun <T : Config> get(name: String, type: KClass<T>): T {
        val config = assets.get<Config>(name)

        require(type.isInstance(config)) {
            "Config '$name' is of type ${config::class.simpleName}, not ${type.simpleName}"
        }

        return type.cast(config)
    }
}
