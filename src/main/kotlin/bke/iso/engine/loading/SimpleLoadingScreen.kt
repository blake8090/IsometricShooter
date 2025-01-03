package bke.iso.engine.loading

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation.fade
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.min

class SimpleLoadingScreen(private val assets: Assets) : LoadingScreen() {

    private val log = KotlinLogging.logger { }

    private val texture = makePixelTexture(Color.WHITE)
    private val batch = SpriteBatch()

    private val duration = 0.25f

    private val stage = Stage(ScreenViewport())
    private val skin = Skin()

    override fun stop() {
        super.stop()
        texture.dispose()
    }

    override val transitionInState: TransitionInState = object : TransitionInState() {

        private var elapsedTime: Float = 0f

        override fun start() {}

        override fun update(deltaTime: Float) {
            elapsedTime += deltaTime
            if (elapsedTime >= duration) {
                log.debug { "Fade in complete" }
                nextState()
            }

            val p = min(1f, elapsedTime / duration)
            val f = fade.apply(p)

            batch.begin()
            batch.withColor(Color(0f, 0f, 0f, f)) {
                batch.draw(texture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            }
            batch.end()
        }
    }

    override val loadingState: LoadingState = object : LoadingState() {

        override fun start() {
            setup()

            val table = Table()
            table.bottom().right()
            table.setFillParent(true)
            table.background = skin.newDrawable("pixel", Color.BLACK)
            stage.addActor(table)

            val label = Label("Loading...", skin)
            label.addAction(
                Actions.repeat(
                    RepeatAction.FOREVER,
                    Actions.sequence(
                        Actions.moveBy(0f, 50f, 0.5f),
                        Actions.moveBy(0f, -50f, 0.5f)
                    )
                )
            )

            table.add(label)
                .padRight(50f)
                .padBottom(50f)
        }

        private fun setup() {
            skin.add("pixel", makePixelTexture())
            skin.add("default", assets.fonts[FontOptions("roboto.ttf", 45f, Color.WHITE)])
            skin.add("default", Label.LabelStyle().apply {
                font = skin.getFont("default")
            })
        }

        override fun update(deltaTime: Float) {
            stage.act(deltaTime)
            stage.viewport.apply()
            stage.draw()
        }
    }

    override val transitionOutState: TransitionOutState = object : TransitionOutState() {

        private var elapsedTime: Float = 0f

        override fun start() {}

        override fun update(deltaTime: Float) {
            elapsedTime += deltaTime
            if (elapsedTime >= duration) {
                log.debug { "Fade out complete" }
                nextState()
            }

            val p = min(1f, elapsedTime / duration)
            val f = 1 - fade.apply(p)

            stage.act(deltaTime)
            stage.viewport.apply()
            stage.actors.forEach { actor ->
                val c = actor.color
                actor.setColor(c.r, c.g, c.b, f)
            }
            stage.draw()

            batch.begin()
            batch.withColor(Color(0f, 0f, 0f, f)) {
                batch.draw(texture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            }
            batch.end()
        }
    }
}
