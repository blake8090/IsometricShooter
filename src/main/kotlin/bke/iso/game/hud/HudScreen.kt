package bke.iso.game.hud

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.get
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

class HudScreen(assets: Assets) : UIScreen(assets) {

    private lateinit var healthBar: HudHealthBar
    private lateinit var weaponLabel: Label

    override fun create() {
        setup()

        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        val table = Table()
            .bottom()
            .left()

        table.add(Label("Health", skin))
            .height(100f)

        healthBar = HudHealthBar(skin).apply {
            barHeight = 32f
            barPadLeft = 20f
            barPadRight = 20f
        }
        table.add(healthBar)
            .width(300f)
            .fillY()

        weaponLabel = Label("No Weapon", skin)
        table.add(weaponLabel)
            .height(50f)
            .expandX()
            .right()
            .padRight(25f)

        root.add(table).grow()
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 25f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newDrawable("pixel", Color.DARK_GRAY)
        })

        skin.add("hud-fps", Label.LabelStyle().apply {
            font = skin.getFont("default")
        })

        skin.add(
            "default", HudHealthBar.HudHealthBarStyle(
                skin.newDrawable("pixel", Color.DARK_GRAY),
                skin.newDrawable("pixel", Color.RED),
                skin.newDrawable("pixel", Color.GREEN)
            )
        )
    }

    fun updateHealth(health: Float, maxHealth: Float) {
        healthBar.maxValue = maxHealth
        healthBar.value = health
    }

    fun setWeaponText(text: String) {
        weaponLabel.setText(text)
    }
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
        style = skin.get<HudHealthBarStyle>(styleName)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        style.background.draw(batch, x, y, width, height)

        val barX = x + barPadLeft
        val barY = y + (height / 2f) - (barHeight / 2f)
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
