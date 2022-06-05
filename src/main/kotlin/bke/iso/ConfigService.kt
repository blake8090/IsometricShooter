package bke.iso

import bke.iso.di.Singleton

data class Config(
    val width: Int = 1280,
    val height: Int = 720,
    val fullScreen: Boolean = false
)

@Singleton
class ConfigService {
    fun resolveConfig(): Config {
        // todo: search files
        return Config()
    }
}
