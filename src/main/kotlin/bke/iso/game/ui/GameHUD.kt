package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.get
import bke.iso.game.weapon.RangedWeaponItem
import bke.iso.game.weapon.RangedWeaponProperties
import bke.iso.game.weapon.WeaponItem
import bke.iso.game.weapon.WeaponProperties
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

class GameHUD(assets: Assets) : UIScreen(assets) {

    private lateinit var healthBar: HudHealthBar
    private lateinit var weaponLabel: Label

    override fun create() {
        setup()

        val table = Table()
        table.left().bottom()
        table.setFillParent(true)
        stage.addActor(table)

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
            .right()

        weaponLabel = Label("No Weapon", skin)
        table.add(weaponLabel)
            .height(50f)
            .expandX()
            .right()
            .padRight(25f)
    }

    fun updateHealth(health: Float, maxHealth: Float) {
        healthBar.maxValue = maxHealth
        healthBar.value = health
    }

    fun updateWeaponText(weaponItem: WeaponItem) {
        val builder = StringBuilder()
        builder.append(weaponItem.name)

        val properties = assets.get<WeaponProperties>(weaponItem.name)
        if (weaponItem is RangedWeaponItem && properties is RangedWeaponProperties) {
            builder.append(": ${weaponItem.ammo}/${properties.magSize}")
        }

        weaponLabel.setText(builder)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 25f, Color.WHITE)])

        skin.add("default", LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newDrawable("pixel", Color.DARK_GRAY)
        })

        skin.add(
            "default", HudHealthBar.HudHealthBarStyle(
                skin.newDrawable("pixel", Color.DARK_GRAY),
                skin.newDrawable("pixel", Color.RED),
                skin.newDrawable("pixel", Color.GREEN)
            )
        )
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
