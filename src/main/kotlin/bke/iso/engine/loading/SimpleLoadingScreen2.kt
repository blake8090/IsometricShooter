package bke.iso.engine.loading

import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.render.withColor
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation.fade
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.min

class SimpleLoadingScreen2 : LoadingScreen2() {

    private val log = KotlinLogging.logger { }

    private val texture = makePixelTexture(Color.WHITE)
    private val batch = SpriteBatch()

    override fun stop() {
        super.stop()
        texture.dispose()
    }

    override val transitionInState: TransitionInState = object : TransitionInState() {

        private var elapsedTime: Float = 0f
        private val duration = 0.5f

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
        override fun start() {}

        override fun update(deltaTime: Float) {
            batch.begin()
            batch.withColor(Color(0f, 0f, 0f, 1f)) {
                batch.draw(texture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            }
            batch.end()
        }
    }

    override val transitionOutState: TransitionOutState = object : TransitionOutState() {

        private var elapsedTime: Float = 0f
        private val duration = 0.5f

        override fun start() {}

        override fun update(deltaTime: Float) {
            elapsedTime += deltaTime
            if (elapsedTime >= duration) {
                log.debug { "Fade out complete" }
                nextState()
            }

            val p = min(1f, elapsedTime / duration)
            val f = 1 - fade.apply(p)

            batch.begin()
            batch.withColor(Color(0f, 0f, 0f, f)) {
                batch.draw(texture, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
            }
            batch.end()
        }
    }
}
