package bke.iso

import bke.iso.ioc.Service

data class Config(
    val width: Int = 800,
    val height: Int = 600,
    val fullScreen: Boolean = true
)

@Service
class ConfigService {
    fun resolveConfig(): Config {
        // todo: search files
        return Config()
    }
}
