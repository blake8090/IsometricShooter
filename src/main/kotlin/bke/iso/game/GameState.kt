package bke.iso.game

import bke.iso.editor.EditorModule
import bke.iso.engine.core.Event
import bke.iso.engine.Engine
import bke.iso.engine.render.occlusion.BuildingLayerOcclusionStrategy
import bke.iso.engine.state.State
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tags
import bke.iso.game.entity.FlyingTurretSystem
import bke.iso.game.entity.RollingTurretSystem
import bke.iso.game.entity.shadow.ShadowSystem
import bke.iso.game.entity.player.Player
import bke.iso.game.entity.player.system.PlayerSystem
import bke.iso.game.entity.TurretSystem
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.system.HealSystem
import bke.iso.game.combat.system.Health
import bke.iso.game.combat.system.HitEffectSystem
import bke.iso.game.entity.door.DoorModule
import bke.iso.game.entity.elevator.ElevatorModule
import bke.iso.game.entity.elevator.ElevatorSystem
import bke.iso.game.hud.HudModule
import bke.iso.game.occlusion.BuildingWallOcclusionStrategy
import bke.iso.game.occlusion.FloorOcclusionStrategy
import bke.iso.game.entity.player.system.PlayerInteractionSystem
import bke.iso.game.entity.player.PlayerDataModule
import bke.iso.game.entity.player.system.PlayerCrosshairLaserSystem
import bke.iso.game.entity.player.system.PlayerWeaponSystem
import bke.iso.game.entity.shadow.ShadowModule
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.weapon.system.BulletSystem
import bke.iso.game.weapon.system.WeaponSystem
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.ExplosionSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
    private val editorModule = EditorModule(engine)

    private var editorEnabled = false

    override val modules = setOf(
        hudModule,
        weaponsModule,
        combatModule,
        shadowModule,
        doorModule,
        elevatorModule,
        editorModule
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

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (editorEnabled) {
                log.info { "Closing editor" }
                engine.events.fire(EditorModule.EditorClosed())
                editorEnabled = false
                engine.events.fire(Engine.GameResumed())
            } else {
                log.info { "Opening editor" }
                engine.events.fire(EditorModule.SceneModeSelected())
                editorEnabled = true
                engine.events.fire(Engine.GamePaused())
            }
        }
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

        engine.world.entities.each<Tags> { actor, tags ->
            log.debug { "Actor $actor has tags ${tags.tags}" }
        }
    }

    private fun initPlayer() {
        engine.world.entities.each { entity: Entity, _: Player ->
            engine.renderer.occlusion.target = entity

            entity.with<Health> { health ->
                hudModule.updateHealthBar(health.value, health.maxValue)
            }
        }
    }

    private fun initMission1StartScene() {
        engine.world.entities.each<Player> { actor, _ ->
            weaponsModule.equip(actor, "pistol")
        }
    }

    private fun initMission1RoofScene() {
        engine.world.entities.each<Player> { actor, _ ->
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
