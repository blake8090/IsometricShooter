package bke.iso.game

import bke.iso.engine.math.Location
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
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import bke.iso.engine.world.Actor
import bke.iso.game.actor.BulletSystem
import bke.iso.game.actor.Factory
import bke.iso.game.actor.MovingPlatformSystem
import bke.iso.game.player.PLAYER_MAX_HEALTH
import bke.iso.game.player.Player
import bke.iso.game.player.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.actor.createMovingPlatform
import bke.iso.game.player.createPlayer
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapLoader
import bke.iso.game.combat.Combat
import bke.iso.game.combat.Health
import bke.iso.game.combat.HealthBar
import bke.iso.game.combat.PlayerDamageEvent
import bke.iso.game.ui.CrosshairCursor
import bke.iso.game.ui.GameHUD
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

class GameState(override val game: Game) : State() {

    private val factory = Factory(game.world)
    private val combat = Combat(game.world, game.events)
    private val crosshair = CrosshairCursor(game.assets, game.input)

    private val gameHud = GameHUD(game.assets)

    override val systems: Set<System> = setOf(
        PlayerSystem(game.input, game.world, game.renderer, combat),
        TurretSystem(game.world, game.collisions, game.renderer.debug, combat),
        BulletSystem(game.world, combat, game.collisions),
        MovingPlatformSystem(game.world),
        ShadowSystem(game.world, game.collisions)
    )

    override suspend fun load() {
        game.assets.register(GameMapLoader())
        game.assets.loadAsync("game")

        loadMap()
        factory.createLampPost(Location(4, 4, 0))
            .move(0f, -0.125f, 0f)
        factory.createLampPost(Location(8, 4, 0))
            .move(0f, -0.125f, 0f)
        factory.createPillar(Location(12, 12, 0))
            .move(-0.5f, 0.5f, 0f)
        factory.createPillar(Location(10, 12, 0))
            .move(-0.5f, 0.5f, 0f)

        bindInput()

        game.renderer.setCursor(crosshair)
        game.ui.setScreen(gameHud)
        gameHud.updateHealth(PLAYER_MAX_HEALTH, PLAYER_MAX_HEALTH)
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
                "shoot" to MouseBinding(Input.Buttons.LEFT, ButtonState.PRESSED),
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
            bind(
                "fly",
                KeyBinding(Input.Keys.Q, ButtonState.DOWN),
                KeyBinding(Input.Keys.E, ButtonState.DOWN)
            )
        }

        with(game.input.controller) {
            bind(
                "run" to ControllerBinding(ControllerButton.LEFTBUMPER.ordinal, ButtonState.DOWN),
                "moveX" to ControllerAxisBinding(ControllerAxis.LEFTX.ordinal),
                "moveY" to ControllerAxisBinding(ControllerAxis.LEFTY.ordinal, true),
                "cursorX" to ControllerAxisBinding(ControllerAxis.RIGHTX.ordinal),
                "cursorY" to ControllerAxisBinding(ControllerAxis.RIGHTY.ordinal),
                "shoot" to ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.PRESSED)
            )
            bind(
                "fly",
                ControllerBinding(ControllerButton.X.ordinal, ButtonState.DOWN),
                ControllerBinding(ControllerButton.A.ordinal, ButtonState.DOWN)
            )
        }
    }

    private fun loadMap() {
        val gameMap = game.assets.get<GameMap>("collision-test")
        for (layer in gameMap.layers) {
            for ((y, row) in layer.tiles.reversed().withIndex()) {
                for ((x, char) in row.withIndex()) {
                    readTile(char, Location(x, y, layer.z))
                }
            }

            for ((y, row) in layer.entities.reversed().withIndex()) {
                for ((x, char) in row.withIndex()) {
                    readEntity(char, Location(x, y, layer.z))
                }
            }
        }
    }

    private fun readTile(char: Char, location: Location) {
        when (char) {
            '1' -> game.world.setTile(location, Sprite("floor", 0f, 16f))
            '2' -> game.world.setTile(location, Sprite("floor2", 0f, 16f))
        }
    }

    private fun readEntity(char: Char, location: Location) {
        when (char) {
            'p' -> {
                val player = game.world.createPlayer(location)
                game.world.createShadow(player)
            }

            '#' -> factory.createWall(location)
            'x' -> factory.createBox(location)
            't' -> factory.createTurret(location)
            '_' -> game.world.createMovingPlatform(location)
            '/' -> factory.createSideFence(location)
            '=' -> factory.createFrontFence(location)
            '|' -> factory.createPillar(location)
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
//        val location = Location()
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
