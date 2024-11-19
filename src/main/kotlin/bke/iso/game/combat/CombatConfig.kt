package bke.iso.game.combat

import bke.iso.engine.asset.config.Config
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("combatConfig")
data class CombatConfig(
    val medkitHealthPercentage: Float,
    val medkitDurationSeconds: Float,
    val hitEffectDurationSeconds: Float
) : Config
