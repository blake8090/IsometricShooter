package bke.iso.game.actor.player

import bke.iso.engine.world.World
import bke.iso.game.actor.Inventory
import bke.iso.game.combat.Health
import bke.iso.game.weapon.Weapon
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Keeps track of the player's data when changing scenes; health, weapons, ammo, etc.
 */
class PlayerDataModule(private val world: World) {

    private val log = KotlinLogging.logger {}

    private var savedData: PlayerData? = null

    fun saveData() {
        val actor = world.actors.find<Player>() ?: return

        val health = checkNotNull(actor.get<Health>()) {
            "Expected Health for actor $actor"
        }

        val numMedkits = actor
            .get<Inventory>()
            ?.numMedkits
            ?: 0

        val weapon = actor
            .get<Inventory>()
            ?.selectedWeapon

        savedData = PlayerData(health.value, health.maxValue, numMedkits, weapon)
        log.debug { "Saved player data" }
    }

    fun loadData() {
        val data = savedData ?: return
        val actor = world.actors.find<Player>() ?: return

        actor.add(Health(data.maxHealth, data.health))
        actor.add(Inventory(data.selectedWeapon, data.numMedkits))

        log.debug { "Loaded player data" }
    }
}

private data class PlayerData(
    val health: Float,
    val maxHealth: Float,
    val numMedkits: Int,
    val selectedWeapon: Weapon?
)
