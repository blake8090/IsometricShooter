package bke.iso.engine//package bke.iso
//
//import bke.iso.engine.AssetService
//import bke.iso.engine.ServiceContainer
//import bke.iso.engine.Renderer
//import bke.iso.engine.SystemService
//import bke.iso.engine.getLogger
////import bke.iso.util.sdf
//import bke.iso.engine.Entity
//import bke.iso.engine.TextureComponent
//import bke.iso.engine.World
//import bke.iso.engine.PositionComponent
//import com.badlogic.gdx.Gdx
//import com.badlogic.gdx.Input
//import kotlinx.coroutines.launch
//import ktx.async.KtxAsync
//import org.slf4j.LoggerFactory
//
//class Engine {
//    private val log = getLogger()
//
//    private val container = ServiceContainer()
//
////    private lateinit var player: Entity
//
//    init {
//        container.registerFromClassPath("bke.iso")
//    }
//
//    fun start() {
//        log.info("Starting up")
//        loadAssets()
//
//        val world = container.getService<World>()
//        world.loadMap("test")
////        player = world.createEntity(TextureComponent("player"))
//    }
//
//    fun update(deltaTime: Float) {
//        container.getService<SystemService>().update(deltaTime)
//
////        updatePlayer(deltaTime)
////        player.findComponent<PositionComponent>()?.let { positionComponent ->
////            val world = container.getService<World>()
////            val pos = world.unitConverter.worldToScreen(positionComponent)
////            container.getService<Renderer>().setCameraPos(pos.x, pos.y)
////        }
//
//        container.getService<Renderer>().render()
//    }
//
////    private fun updatePlayer(deltaTime: Float) {
////        var dx = 0f
////        var dy = 0f
////        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
////            dy = -1f
////        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
////            dy = 1f
////        }
////
////        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
////            dx = -1f
////        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
////            dx = 1f
////        }
////
////        val speed = 2f
////        player.move((speed * dx) * deltaTime, (speed * dy) * deltaTime)
////    }
//
//    fun stop() {
//        log.info("Shutting down")
//    }
//
//    fun resolveConfig(): Config =
//        container.getService<ConfigService>()
//            .resolveConfig()
//
//    private fun loadAssets() {
//        container.getService<AssetService>().apply {
//            // TODO: use globals
//            setupAssetLoadersInPackage("bke.iso")
//            KtxAsync.launch {
//                loadAssets("assets")
//            }
//        }
//    }
//}