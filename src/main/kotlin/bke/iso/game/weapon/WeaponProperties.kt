package bke.iso.game.weapon

import bke.iso.engine.asset.cache.AssetCache
import bke.iso.engine.asset.cache.LoadedAsset
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
sealed class WeaponProperties {
    abstract val name: String
}

@Serializable
@SerialName("melee")
data class MeleeWeaponProperties(
    override val name: String
) : WeaponProperties()

@Serializable
enum class FireType {
    @SerialName("semi")
    SEMI,

    @SerialName("auto")
    AUTO
}

@Serializable
@SerialName("ranged")
data class RangedWeaponProperties(
    override val name: String,
    val fireType: FireType,
    val damage: Float,
    val magSize: Int,
    val fireRate: Float,
    val velocity: Float,
    val spread: Float,
    val recoil: Float
) : WeaponProperties()

class WeaponPropertiesCache(private val serializer: Serializer) : AssetCache<WeaponProperties>() {

    override val extensions = setOf("weapon")

    override suspend fun loadAssets(file: File): List<LoadedAsset<WeaponProperties>> =
        withContext(Dispatchers.IO) {
            val properties = serializer.read<WeaponProperties>(file.readText())
            listOf(LoadedAsset(properties.name, properties))
        }
}
