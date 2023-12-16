package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.ControllerAxisBinding
import bke.iso.engine.input.ControllerBinding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.world.Actor
import bke.iso.game.actor.MovingPlatform
import bke.iso.game.actor.MovingPlatformSystem
import bke.iso.game.player.Player
import bke.iso.game.player.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.combat.CombatModule
import bke.iso.game.hud.HudModule
import bke.iso.game.player.PlayerWeaponSystem
import bke.iso.game.player.RELOAD_ACTION
import bke.iso.game.player.SHOOT_ACTION
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.weapon.BulletSystem
import bke.iso.game.weapon.WeaponPropertiesCache
import bke.iso.game.weapon.WeaponSystem
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.Input
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

const val PLAYER_MAX_HEALTH: Float = 5f

class GameState(override val game: Game) : State() {

    private val combatModule = CombatModule(game.world, game.events)
    private val weaponsModule = WeaponsModule(game.assets, game.world)
    private val hudModule = HudModule(game.world, game.assets, weaponsModule)

    override val modules = setOf(hudModule, weaponsModule, combatModule)

    override val systems: Set<System> = setOf(
        WeaponSystem(game.world, game.assets),
        PlayerWeaponSystem(game.world, game.input, game.renderer, game.events, weaponsModule),
        PlayerSystem(game.input, game.world, game.renderer),
        TurretSystem(game.world, game.collisions, game.renderer.debug, game.events, weaponsModule),
        BulletSystem(game.world, combatModule, game.collisions),
        MovingPlatformSystem(game.world),
        ShadowSystem(game.world, game.collisions)
    )

    private val crosshair = CrosshairPointer(game.assets, game.input, game.world, game.renderer, weaponsModule)

    override suspend fun load() {
        game.assets.register(WeaponPropertiesCache(game.serializer))
        game.assets.loadAsync("game")

        game.scenes.load("building.scene")
        // hack to make the moving platform work in building.scene
        game.world.actors.each<MovingPlatform> { actor, _ ->
            actor.add(MovingPlatform(speed = 1f, maxZ = 4f, minZ = 2f))
        }

        bindInput()

        game.renderer.setPointer(crosshair)
        hudModule.init(game.ui, PLAYER_MAX_HEALTH, PLAYER_MAX_HEALTH)

        game.world.actors.each { actor: Actor, _: Player ->
            game.world.createShadow(actor)
            weaponsModule.equip(actor, "rifle")
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
                "crouch" to KeyBinding(Input.Keys.C, ButtonState.PRESSED)
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
                "crouch" to ControllerBinding(ControllerButton.LEFTSTICK.ordinal, ButtonState.PRESSED)
            )
        }
    }

//    private fun generatePrefabs() {
//        generatePrefab("lamppost", Factory(game.world).createLampPost(Location()))
//        generatePrefab("player", game.world.createPlayer(location))
//        //generatePrefab(game.world.createShadow(player))
//        generatePrefab("wall", factory.createWall(location))
//        generatePrefab("box", factory.createBox(location))
//        generatePrefab("turret", factory.createTurret(location))
//        generatePrefab("platform", game.world.createMovingPlatform(location))
//        generatePrefab("side-fence", factory.createSideFence(location))
//        generatePrefab("front-fence", factory.createFrontFence(location))
//        generatePrefab("pillar", factory.createPillar(location))
//        generatePrefab("bullet", game.world.createBullet(Actor(""), Vector3(), BulletType.PLAYER))
//    }
//
//    private fun generatePrefab(name: String, actor: Actor) {
//        val prefab = ActorPrefab(name, actor.components.values.toList())
//        val json = game.serializer.write(prefab)
//        println("PREFAB $name\n$json\n")
//        game.world.actors.delete(actor)
//    }
}
