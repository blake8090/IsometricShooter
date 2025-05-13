package bke.iso.game.entity.player

import bke.iso.engine.core.Module
import bke.iso.engine.world.World
import bke.iso.game.entity.Inventory
import bke.iso.game.combat.system.Health
import bke.iso.game.weapon.system.Weapon
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Keeps track of the player's data when changing scenes; health, weapons, ammo, etc.
 */
class PlayerDataModule(private val world: World) : Module {

    override val alwaysActive: Boolean = false

    private val log = KotlinLogging.logger {}

    private var savedData: PlayerData? = null

    fun saveData() {
        val entity = world.entities.find<Player>() ?: return

        val health = checkNotNull(entity.get<Health>()) {
            "Expected Health for entity $entity"
        }

        val numMedkits = entity
            .get<Inventory>()
            ?.numMedkits
            ?: 0

        val weapon = entity
            .get<Inventory>()
            ?.selectedWeapon

        savedData = PlayerData(health.value, health.maxValue, numMedkits, weapon)
        log.debug { "Saved player data" }
    }

    fun loadData() {
        val data = savedData ?: return
        val entity = world.entities.find<Player>() ?: return

        entity.add(Health(data.maxHealth, data.health))
        entity.add(Inventory(data.selectedWeapon, data.numMedkits))

        log.debug { "Loaded player data" }
    }
}

private data class PlayerData(
    val health: Float,
    val maxHealth: Float,
    val numMedkits: Int,
    val selectedWeapon: Weapon?
)
