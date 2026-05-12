package bke.iso.game.hud

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.core.Event
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.util.get
import bke.iso.engine.ui.scene2d.Scene2dView
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.Event as Scene2dEvent

class HudView(assets: Assets) : Scene2dView(assets) {

    private lateinit var healthBar: HealthBarComponent
    private lateinit var medkitsLabel: Label
    private lateinit var weaponLabel: Label
    private lateinit var interactionLabel: Label

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

        interactionLabel = Label("", skin, "interaction")
        interactionLabel.isVisible = false
        root.add(interactionLabel)
            .expand()
            .bottom()
            .left()
            .pad(20f)

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

        skin.add("interaction", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newDrawable("pixel", Color(0.353f, 0.439f, 0.522f, 0.75f))
        })

        skin.add(
            "default", HealthBarComponent.HealthBarStyle(
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

        healthBar = HealthBarComponent(skin, barHeight = 32f)
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

    override fun handleScene2dEvent(event: Scene2dEvent) {
    }

    override fun handleEvent(event: Event) {
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

    fun setInteractionText(text: String) {
        interactionLabel.isVisible = true
        interactionLabel.setText(text)
    }

    fun hideInteractionText() {
        interactionLabel.isVisible = false
    }
}

private class HealthBarComponent(
    skin: Skin,
    styleName: String = "default",
    var barHeight: Float = 0f
) : Actor() {

    private val style: HealthBarStyle

    var maxValue = 1f
    var value = 1f

    init {
        style = skin.get<HealthBarStyle>(styleName)
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

    class HealthBarStyle(
        val background: Drawable,
        val barBackground: Drawable,
        val barForeground: Drawable
    )
}
