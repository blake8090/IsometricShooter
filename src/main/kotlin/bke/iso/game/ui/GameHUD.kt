package bke.iso.game.ui

import bke.iso.engine.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import mu.KotlinLogging
import kotlin.reflect.cast

private const val HUD_HEALTH_BAR_NAME = "healthBar"

class GameHUD(private val assets: Assets) : UIScreen() {

    private val log = KotlinLogging.logger {}
    private val skin = Skin()

    override fun create() {
        super.create()
        setup()

        val table = Table()
//        table.debug = true
        table.left().bottom()
        table.setFillParent(true)
        stage.addActor(table)

        table.add(Label("Health", skin))
            .height(100f)

        table.add(HudHealthBar(skin).apply {
            name = HUD_HEALTH_BAR_NAME
            barHeight = 32f
            barPadLeft = 20f
            barPadRight = 20f
        })
            .width(300f)
            .fillY()
            .right()
    }

    private fun setup() {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        skin.add("white", Texture(pixmap))

        // TODO: reload fonts in skin when resizing?
        skin.add("default", assets.fonts[FontOptions("roboto", 25f, Color.WHITE)])

        skin.add("default", LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newDrawable("white", Color.DARK_GRAY)
        })

        skin.add(
            "default", HudHealthBar.HudHealthBarStyle(
                skin.newDrawable("white", Color.DARK_GRAY),
                skin.newDrawable("white", Color.RED),
                skin.newDrawable("white", Color.GREEN)
            )
        )
    }

    override fun handleEvent(event: Event) {
        if (event is UpdateEvent) {
            log.debug { "updating hud: $event" }
            val actor = HudHealthBar::class.cast(stage.root.findActor(HUD_HEALTH_BAR_NAME))
            actor.maxValue = event.maxHealth
            actor.value = event.currentHealth
        }
    }

    override fun dispose() {
        super.dispose()
        skin.dispose()
    }

    class UpdateEvent(
        val maxHealth: Float,
        val currentHealth: Float = maxHealth
    ) : Event()
}

private class HudHealthBar(
    skin: Skin,
    styleName: String = "default"
) : Actor() {

    private val style: HudHealthBarStyle

    var barHeight: Float = 0f
    var barPadLeft: Float = 0f
    var barPadRight: Float = 0f

    var maxValue = 1f
    var value = 1f

    init {
        style = skin.get(styleName, HudHealthBarStyle::class.java)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        style.background.draw(batch, x, y, width, height)

        val barX = x + barPadLeft
        val barY = (height / 2f) - (barHeight / 2f)
        val barWidth = width - barPadLeft - barPadRight
        style.barBackground.draw(batch, barX, barY, barWidth, barHeight)

        val ratio = value / maxValue
        val fgWidth = barWidth * ratio
        style.barForeground.draw(batch, barX, barY, fgWidth, barHeight)
    }

    class HudHealthBarStyle(
        val background: Drawable,
        val barBackground: Drawable,
        val barForeground: Drawable
    )
}