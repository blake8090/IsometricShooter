package bke.iso.game

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.ControllerAxisBinding
import bke.iso.engine.input.ControllerBinding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.FlyingTurretSystem
import bke.iso.game.actor.MovingPlatformSystem
import bke.iso.game.actor.RollingTurretSystem
import bke.iso.game.shadow.ShadowSystem
import bke.iso.game.player.Player
import bke.iso.game.player.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.combat.CombatModule
import bke.iso.game.combat.HealSystem
import bke.iso.game.combat.Health
import bke.iso.game.combat.HitEffectSystem
import bke.iso.game.door.Door
import bke.iso.game.door.DoorChangeSceneAction
import bke.iso.game.door.DoorModule
import bke.iso.game.hud.HudModule
import bke.iso.game.player.PlayerStateModule
import bke.iso.game.player.PlayerWeaponSystem
import bke.iso.game.player.RELOAD_ACTION
import bke.iso.game.player.SHOOT_ACTION
import bke.iso.game.shadow.ShadowModule
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.weapon.system.BulletSystem
import bke.iso.game.weapon.WeaponPropertiesCache
import bke.iso.game.weapon.system.WeaponSystem
import bke.iso.game.weapon.WeaponsModule
import bke.iso.game.weapon.system.ExplosionSystem
import com.badlogic.gdx.Input
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton
import mu.KotlinLogging

class GameState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    private val combatModule = CombatModule(game.world, game.events)
    private val weaponsModule = WeaponsModule(game.assets, game.world)
    private val doorModule = DoorModule(game.world, game.ui, game.events)
    private val hudModule = HudModule(game.world, game.assets, weaponsModule, doorModule)
    private val shadowModule = ShadowModule(game.world)
    private val playerStateModule = PlayerStateModule(game.world)

    override val modules = setOf(
        hudModule,
        weaponsModule,
        combatModule,
        shadowModule,
        doorModule
    )

    override val systems = linkedSetOf(
        WeaponSystem(game.world, game.assets),
        PlayerWeaponSystem(game.world, game.input, game.renderer, game.events, weaponsModule),
        PlayerSystem(game.input, game.world, game.renderer, game.collisions, combatModule, doorModule),
        TurretSystem(game.world, game.collisions, game.renderer.debug, game.events, weaponsModule),
        RollingTurretSystem(game.world, game.collisions, game.renderer, game.events, weaponsModule),
        FlyingTurretSystem(game.world, game.collisions, game.renderer, game.events, weaponsModule),
        BulletSystem(game.world, combatModule, game.collisions),
        ExplosionSystem(game.world),
        MovingPlatformSystem(game.world),
        ShadowSystem(game.world, game.collisions),
        HealSystem(game.world, game.events),
        HitEffectSystem(game.world)
    )

    private val crosshair = CrosshairPointer(game.assets, game.input, game.world, game.renderer, weaponsModule)

    override suspend fun load() {
        bindInput()

        game.assets.addCache(WeaponPropertiesCache(game.serializer))
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

    private fun bindInput() {
        with(game.input.keyMouse) {
            bind(
                "toggleDebug" to KeyBinding(Input.Keys.M, ButtonState.PRESSED),
                "placeBouncyBall" to KeyBinding(Input.Keys.Z, ButtonState.PRESSED),
                "run" to KeyBinding(Input.Keys.SHIFT_LEFT, ButtonState.DOWN),
                "jump" to KeyBinding(Input.Keys.SPACE, ButtonState.PRESSED),
                SHOOT_ACTION to MouseBinding(Input.Buttons.LEFT, ButtonState.DOWN),
                RELOAD_ACTION to KeyBinding(Input.Keys.R, ButtonState.PRESSED),
                "crouch" to KeyBinding(Input.Keys.C, ButtonState.PRESSED),
                "useMedkit" to KeyBinding(Input.Keys.Q, ButtonState.PRESSED),
                "openDoor" to KeyBinding(Input.Keys.E, ButtonState.PRESSED)
            )
            bind(
                "moveY",
                KeyBinding(Input.Keys.S, ButtonState.DOWN),
                KeyBinding(Input.Keys.W, ButtonState.DOWN)
            )
            bind(
                "moveX",
                KeyBinding(Input.Keys.A, ButtonState.DOWN),
                KeyBinding(Input.Keys.D, ButtonState.DOWN)
            )
        }

        with(game.input.controller) {
            bind(
                "run" to ControllerBinding(ControllerButton.LEFTBUMPER.ordinal, ButtonState.DOWN),
                "moveX" to ControllerAxisBinding(ControllerAxis.LEFTX.ordinal),
                "moveY" to ControllerAxisBinding(ControllerAxis.LEFTY.ordinal, true),
                "cursorX" to ControllerAxisBinding(ControllerAxis.RIGHTX.ordinal),
                "cursorY" to ControllerAxisBinding(ControllerAxis.RIGHTY.ordinal),
                "jump" to ControllerBinding(ControllerButton.A.ordinal, ButtonState.PRESSED),
                SHOOT_ACTION to ControllerAxisBinding(ControllerAxis.TRIGGERRIGHT.ordinal),//ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.DOWN),
                RELOAD_ACTION to ControllerBinding(ControllerButton.X.ordinal, ButtonState.PRESSED),
                "crouch" to ControllerBinding(ControllerButton.LEFTSTICK.ordinal, ButtonState.PRESSED),
                "useMedkit" to ControllerBinding(ControllerButton.DPAD_UP.ordinal, ButtonState.PRESSED),
                "openDoor" to ControllerBinding(ControllerButton.Y.ordinal, ButtonState.PRESSED)
            )
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

        game.world.actors.each<Door> { actor, _ ->
            actor.add(DoorChangeSceneAction("city2.scene"))
            log.debug { "Set up door $actor" }
        }
    }

    data class LoadSceneEvent(
        val sceneName: String,
        val savePlayerData: Boolean
    ) : Event
}
