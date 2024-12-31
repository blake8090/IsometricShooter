package bke.iso.engine.asset.config

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface Config

class ConfigAssetCache(private val serializer: Serializer) : AssetCache<Config>() {
    override val extensions: Set<String> = setOf("cfg")

    override suspend fun load(file: File) {
        withContext(Dispatchers.IO) {
            val config = serializer.read<Config>(file.readText())
            store(file, file.name, config)
        }
    }
}
