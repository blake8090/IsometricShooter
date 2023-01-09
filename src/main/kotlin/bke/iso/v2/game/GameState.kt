package bke.iso.v2.game

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.TileService
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.state.State
import bke.iso.v2.engine.system.System

@Transient
class GameState(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityFactory: EntityFactory
) : State() {
    override val systems = emptySet<System>()

    override fun start() {
        log.debug("on start")
        assetService.load("assets")
        loadMap()
    }

    private fun loadMap() {
        val mapData = assetService.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            tileService.setTile(tile, location)
        }

        mapData.walls.forEach { location ->
            entityFactory.createWall(location.x.toFloat(), location.y.toFloat())
        }

        mapData.boxes.forEach { location ->
            entityFactory.createBox(location.x.toFloat(), location.y.toFloat())
        }

        mapData.turrets.forEach { location ->
            entityFactory.createTurret(location.x.toFloat(), location.y.toFloat())
        }

        mapData.players.forEach { location ->
            entityFactory.createPlayer(location.x.toFloat(), location.y.toFloat())
        }
    }
}
