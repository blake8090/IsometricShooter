package bke.iso.game.weapon

import bke.iso.engine.asset.cache.AssetCache
import bke.iso.engine.asset.cache.LoadedAsset
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Weapon(
    val name: String,
    val damage: Float,
    val magSize: Int,
    val fireRate: Float,
    val bulletVelocity: Float,
    val spread: Float,
    val recoil: Float
)

class WeaponCache(private val serializer: Serializer) : AssetCache<Weapon>() {

    override val extensions = setOf("weapon")

    override suspend fun loadAssets(file: File): List<LoadedAsset<Weapon>> =
        withContext(Dispatchers.IO) {
            val weapon = serializer.read<Weapon>(file.readText())
            listOf(LoadedAsset(file.nameWithoutExtension, weapon))
        }
}
