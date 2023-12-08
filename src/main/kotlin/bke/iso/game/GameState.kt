package bke.iso.game

import bke.iso.engine.math.toScreen
import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.ControllerAxisBinding
import bke.iso.engine.input.ControllerBinding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.render.DrawActorEvent
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import bke.iso.engine.world.Actor
import bke.iso.game.actor.BulletSystem
import bke.iso.game.actor.MovingPlatform
import bke.iso.game.actor.MovingPlatformSystem
import bke.iso.game.player.PLAYER_MAX_HEALTH
import bke.iso.game.player.Player
import bke.iso.game.player.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.combat.Combat
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import bke.iso.game.combat.PlayerDamageEvent
import bke.iso.game.ui.CrosshairPointer
import bke.iso.game.ui.GameHUD
import bke.iso.game.weapon.Bullet2System
import bke.iso.game.weapon.Inventory
import bke.iso.game.weapon.WeaponPropertiesCache
import bke.iso.game.weapon.WeaponSystem
import bke.iso.game.weapon.Weapons
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

class GameState(override val game: Game) : State() {

    private val gameHud = GameHUD(game.assets)
    private val crosshair = CrosshairPointer(game.assets, game.input, game.world, game.renderer)

    private val combat = Combat(game.world, game.events)
    private val weapons = Weapons(game.assets, game.world)

    override val systems: Set<System> = setOf(
        WeaponSystem(game.world),
        PlayerSystem(game.input, game.world, game.renderer, weapons),
        TurretSystem(game.world, game.collisions, game.renderer.debug, combat),
        BulletSystem(game.world, combat, game.collisions),
        Bullet2System(game.world, combat, game.collisions),
        MovingPlatformSystem(game.world),
        ShadowSystem(game.world, game.collisions)
    )

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
        game.ui.setScreen(gameHud)
        gameHud.updateHealth(PLAYER_MAX_HEALTH, PLAYER_MAX_HEALTH)

        game.world.actors.each { actor: Actor, _: Player ->
            game.world.createShadow(actor)
            weapons.equip(actor, "rifle")
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        val weaponItem = game.world.actors
            .find<Player>()
            ?.get<Inventory>()
            ?.selectedWeapon
            ?: return
        gameHud.updateWeaponText(weaponItem)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is DrawActorEvent -> {
                drawHealthBar(event.actor, event.batch)
            }

            is PlayerDamageEvent -> {
                gameHud.updateHealth(event.health, PLAYER_MAX_HEALTH)
            }
        }
    }

    private fun bindInput() {
        with(game.input.keyMouse) {
            bind(
                "toggleDebug" to KeyBinding(Input.Keys.M, ButtonState.PRESSED),
                "placeBouncyBall" to KeyBinding(Input.Keys.Z, ButtonState.PRESSED),
                "run" to KeyBinding(Input.Keys.SHIFT_LEFT, ButtonState.DOWN),
                "shootOnce" to MouseBinding(Input.Buttons.LEFT, ButtonState.PRESSED),
                "shoot" to MouseBinding(Input.Buttons.LEFT, ButtonState.DOWN),
                "jump" to KeyBinding(Input.Keys.SPACE, ButtonState.PRESSED)
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
                "shootOnce" to ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.PRESSED),
                "shoot" to ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.DOWN),
                "jump" to ControllerBinding(ControllerButton.A.ordinal, ButtonState.PRESSED)
            )
        }
    }

    private fun drawHealthBar(actor: Actor, batch: PolygonSpriteBatch) {
        // we already display the player's health in the HUD!
        if (actor.has<Player>()) {
            return
        }

        val healthBarWidth = 32f
        val healthBarHeight = 8f

        val health = actor.get<Health>() ?: return
        val healthBar = actor.get<HealthBar>() ?: return

        val pixel = makePixelTexture()
        val pos = toScreen(actor.pos)
            .sub(healthBar.offsetX, healthBar.offsetY)

        batch.withColor(Color.RED) {
            batch.draw(pixel, pos.x, pos.y, healthBarWidth, healthBarHeight)
        }

        batch.withColor(Color.GREEN) {
            val ratio = health.value / health.maxValue
            val width = healthBarWidth * ratio
            batch.draw(pixel, pos.x, pos.y, width, healthBarHeight)
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
