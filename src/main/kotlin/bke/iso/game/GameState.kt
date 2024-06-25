package bke.iso.game

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tags
import bke.iso.game.actor.FlyingTurretSystem
import bke.iso.game.actor.RollingTurretSystem
import bke.iso.game.shadow.ShadowSystem
import bke.iso.game.player.Player
import bke.iso.game.player.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.HealSystem
import bke.iso.game.combat.Health
import bke.iso.game.combat.HitEffectSystem
import bke.iso.game.door.DoorModule
import bke.iso.game.elevator.ElevatorModule
import bke.iso.game.elevator.ElevatorSystem
import bke.iso.game.hud.HudModule
import bke.iso.game.player.PlayerBuildingVisibilitySystem
import bke.iso.game.player.PlayerInteractionSystem
import bke.iso.game.player.PlayerStateModule
import bke.iso.game.player.PlayerWeaponSystem
import bke.iso.game.shadow.ShadowModule
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.weapon.system.BulletSystem
import bke.iso.game.weapon.system.WeaponSystem
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.ExplosionSystem
import mu.KotlinLogging

class GameState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val combatModule = CombatModule(game.world, game.events)
    private val weaponsModule = WeaponsModule(game.assets, game.world)
    private val doorModule = DoorModule(game.world, game.ui, game.events)
    private val hudModule = HudModule(game.world, game.assets, weaponsModule)
    private val shadowModule = ShadowModule(game.world)
    private val playerStateModule = PlayerStateModule(game.world)
    private val elevatorModule = ElevatorModule(game.collisions)

    override val modules = setOf(
        hudModule,
        weaponsModule,
        combatModule,
        shadowModule,
        doorModule,
        elevatorModule
    )

    override val systems = linkedSetOf(
        WeaponSystem(game.world, game.assets),
        PlayerWeaponSystem(game.world, game.input, game.renderer, game.events, weaponsModule),
        PlayerSystem(game.input, game.world, game.renderer, game.collisions, combatModule),
        PlayerInteractionSystem(game.world, game.input, hudModule, doorModule, elevatorModule),
        TurretSystem(game.world, game.collisions, game.renderer.debug, game.events, weaponsModule),
        RollingTurretSystem(game.world, game.collisions, game.renderer, game.events, weaponsModule),
        FlyingTurretSystem(game.world, game.collisions, game.renderer, game.events, weaponsModule),
        BulletSystem(game.world, combatModule, game.collisions),
        ExplosionSystem(game.world),
        ShadowSystem(game.world, game.collisions),
        HealSystem(game.world, game.events),
        HitEffectSystem(game.world),
        PlayerBuildingVisibilitySystem(game.world),
        ElevatorSystem(game.world)
    )

    private val crosshair = CrosshairPointer(game.assets, game.input, game.world, game.renderer, weaponsModule)

    override suspend fun load() {
        game.assets.loadAsync("game")
        game.assets.shaders.compileAll()

        hudModule.init(game.ui)
        game.renderer.pointer.set(crosshair)

        game.renderer.debug.enableCategories("vision", "turret", "collisions")
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        if (event is LoadSceneEvent) {
            loadScene(event.sceneName, event.savePlayerData)
        }
    }

    private fun loadScene(name: String, savePlayerData: Boolean) {
        if (savePlayerData) {
            playerStateModule.saveState()
        }

        game.scenes.load(name)

        if (savePlayerData) {
            playerStateModule.loadState()
        }

        initPlayer()

        when (name) {
            "mission-01-roof.scene" -> initMission1RoofScene()
        }

        game.world.actors.each<Tags> { actor, tags ->
            log.debug { "Actor $actor has tags ${tags.tags}" }
        }
    }

    private fun initPlayer() {
        game.world.actors.each { actor: Actor, _: Player ->
            game.renderer.setOcclusionTarget(actor)

            actor.with<Health> { health ->
                hudModule.updateHealthBar(health.value, health.maxValue)
            }
        }
    }

    private fun initMission1RoofScene() {
        game.world.actors.each<Player> { actor, _ ->
            weaponsModule.equip(actor, "pistol")
        }
    }

    data class LoadSceneEvent(
        val sceneName: String,
        val savePlayerData: Boolean
    ) : Event
}
