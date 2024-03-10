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
    private lateinit var medkitsLabel: Label
    private lateinit var weaponLabel: Label

    override fun create() {
        setup()

        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        root.add(createHealthTable())
            .expand()
            .bottom()
            .left()
            .pad(20f)
        setMedkitsText(0)

        root.add(createSelectedWeaponTable())
            .bottom()
            .pad(20f)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 25f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
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

    private fun createHealthTable(): Table {
        val table = Table()
        table.background = skin.newDrawable("pixel", Color.DARK_GRAY)
        table.pad(10f)

        table.add(Label("Health", skin))
            .left()

        healthBar = HudHealthBar(skin, barHeight = 32f)
        table.add(healthBar)
            .width(200f)
            .spaceLeft(10f)

        table.row()

        medkitsLabel = Label("", skin)
        table.add(medkitsLabel)
            .left()
            .colspan(2)

        return table
    }

    private fun createSelectedWeaponTable(): Table {
        val table = Table()
        table.background = skin.newDrawable("pixel", Color.DARK_GRAY)
        table.pad(20f)

        weaponLabel = Label("", skin)
        table.add(weaponLabel)

        return table
    }

    fun setHealth(health: Float) {
        healthBar.value = health
    }

    fun setMaxHealth(maxHealth: Float) {
        healthBar.maxValue = maxHealth
    }

    fun setWeaponText(text: String) {
        weaponLabel.setText(text)
    }

    fun setMedkitsText(numMedkits: Int) {
        medkitsLabel.setText("Medkits: $numMedkits")
    }
}

private class HudHealthBar(
    skin: Skin,
    styleName: String = "default",
    var barHeight: Float = 0f
) : Actor() {

    private val style: HudHealthBarStyle

    var maxValue = 1f
    var value = 1f

    init {
        style = skin.get<HudHealthBarStyle>(styleName)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        style.background.draw(batch, x, y, width, height)

        val barX = x
        val barY = y + (height / 2f) - (barHeight / 2f)
        val barWidth = width
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
