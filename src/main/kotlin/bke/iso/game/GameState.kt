package bke.iso.game

import bke.iso.engine.core.Event
import bke.iso.engine.Engine
import bke.iso.engine.render.occlusion.BuildingLayerOcclusionStrategy
import bke.iso.engine.state.State
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tags
import bke.iso.game.actor.FlyingTurretSystem
import bke.iso.game.actor.RollingTurretSystem
import bke.iso.game.actor.shadow.ShadowSystem
import bke.iso.game.actor.player.Player
import bke.iso.game.actor.player.system.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.system.HealSystem
import bke.iso.game.combat.system.Health
import bke.iso.game.combat.system.HitEffectSystem
import bke.iso.game.actor.door.DoorModule
import bke.iso.game.actor.elevator.ElevatorModule
import bke.iso.game.actor.elevator.ElevatorSystem
import bke.iso.game.hud.HudModule
import bke.iso.game.occlusion.BuildingWallOcclusionStrategy
import bke.iso.game.occlusion.FloorOcclusionStrategy
import bke.iso.game.actor.player.system.PlayerInteractionSystem
import bke.iso.game.actor.player.PlayerDataModule
import bke.iso.game.actor.player.system.PlayerCrosshairLaserSystem
import bke.iso.game.actor.player.system.PlayerWeaponSystem
import bke.iso.game.actor.shadow.ShadowModule
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.weapon.system.BulletSystem
import bke.iso.game.weapon.system.WeaponSystem
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.ExplosionSystem
import io.github.oshai.kotlinlogging.KotlinLogging

class GameState(override val engine: Engine) : State() {

    private val log = KotlinLogging.logger {}

    private val combatModule = CombatModule(
        engine.world,
        engine.events,
        engine.assets.configs
    )
    private val weaponsModule = WeaponsModule(
        engine.assets,
        engine.world
    )
    private val doorModule = DoorModule(
        engine.world,
        engine.loadingScreens,
        engine.events,
        engine.assets
    )
    private val hudModule = HudModule(
        engine.world,
        engine.assets,
        weaponsModule,
    )
    private val shadowModule = ShadowModule(engine.world)
    private val playerDataModule = PlayerDataModule(engine.world)
    private val elevatorModule = ElevatorModule(engine.collisions)

    override val modules = setOf(
        hudModule,
        weaponsModule,
        combatModule,
        shadowModule,
        doorModule,
        elevatorModule
    )

    override val systems = linkedSetOf(
        WeaponSystem(
            engine.world,
            engine.assets
        ),
        PlayerWeaponSystem(
            engine.world,
            engine.input,
            engine.renderer,
            engine.events,
            weaponsModule
        ),
        PlayerSystem(
            engine.input,
            engine.world,
            engine.renderer,
            engine.collisions,
            combatModule,
            weaponsModule,
            engine.assets.configs
        ),
        PlayerInteractionSystem(
            engine.world,
            engine.input,
            hudModule,
            doorModule,
            elevatorModule
        ),
        PlayerCrosshairLaserSystem(
            engine.world,
            engine.renderer,
            engine.collisions,
            weaponsModule
        ),
        TurretSystem(
            engine.world,
            engine.collisions,
            engine.renderer.debug,
            engine.events,
            weaponsModule
        ),
        RollingTurretSystem(
            engine.world,
            engine.collisions,
            engine.renderer,
            engine.events,
            weaponsModule
        ),
        FlyingTurretSystem(
            engine.world,
            engine.collisions,
            engine.renderer,
            engine.events,
            weaponsModule
        ),
        BulletSystem(
            engine.world,
            combatModule,
            engine.collisions
        ),
        ExplosionSystem(engine.world),
        ShadowSystem(
            engine.world,
            engine.collisions
        ),
        HealSystem(
            engine.world,
            engine.events
        ),
        HitEffectSystem(engine.world),
        ElevatorSystem(engine.world)
    )

    private val crosshair = CrosshairPointer(
        engine.assets,
        engine.input,
        engine.world,
        engine.renderer,
        weaponsModule
    )

    override suspend fun load() {
        engine.ui.clearScene2dViews()
        hudModule.init(engine.ui)
        engine.renderer.pointer.set(crosshair)

        engine.renderer.debug.enableCategories(
//            "vision",
//            "turret",
            "collisions", // TODO: use constants instead
            "weapon"
        )
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        if (event is LoadSceneEvent) {
            loadScene(event.sceneName, event.savePlayerData)
        }
    }

    private fun loadScene(name: String, savePlayerData: Boolean) {
        if (savePlayerData) {
            playerDataModule.saveData()
        }

        engine.scenes.load(name)

        if (savePlayerData) {
            playerDataModule.loadData()
        }

        initPlayer()

        when (name) {
            "mission-01-start.scene" -> initMission1StartScene()
            "mission-01-roof.scene" -> initMission1RoofScene()
            "mission-01-interior.scene" -> initMission1InteriorScene()
        }

        engine.world.actors.each<Tags> { actor, tags ->
            log.debug { "Actor $actor has tags ${tags.tags}" }
        }
    }

    private fun initPlayer() {
        engine.world.actors.each { actor: Actor, _: Player ->
            engine.renderer.occlusion.target = actor

            actor.with<Health> { health ->
                hudModule.updateHealthBar(health.value, health.maxValue)
            }
        }
    }

    private fun initMission1StartScene() {
        engine.world.actors.each<Player> { actor, _ ->
            weaponsModule.equip(actor, "pistol")
        }
    }

    private fun initMission1RoofScene() {
        engine.world.actors.each<Player> { actor, _ ->
            weaponsModule.equip(actor, "pistol")
        }
    }

    private fun initMission1InteriorScene() {
        engine.renderer.occlusion.apply {
            resetStrategies()
            addStrategy(BuildingWallOcclusionStrategy(engine.world))
            addStrategy(FloorOcclusionStrategy(floorHeight = 2f))
            removeStrategy(BuildingLayerOcclusionStrategy::class)
        }
    }

    data class LoadSceneEvent(
        val sceneName: String,
        val savePlayerData: Boolean
    ) : Event
}
