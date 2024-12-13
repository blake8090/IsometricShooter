package bke.iso.editor.actor

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class ActorTabView(
    private val skin: Skin,
    private val assets: Assets,
    private val stage: Stage
) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val spriteView = SpriteView(assets)

    fun create() {
        menuBar.background = skin.getDrawable("bg")
        menuBar.add(createMenuButton("New"))
        menuBar.add(createMenuButton("Open").apply {
            onChanged {
                fire(OpenActorEvent())
            }
        })
        menuBar.add(createMenuButton("Save"))
        menuBar.add(createMenuButton("Save As"))

        spriteView.background = skin.getDrawable("bg")
        mainView.add(spriteView).grow()
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }

    fun setSprite(sprite: Sprite) {
        spriteView.selectedSprite = sprite
    }

    fun draw(sprite: Sprite) {
        val tex = assets.get<Texture>(sprite.texture)
        stage.batch.begin()
        stage.batch.draw(tex, 0f, 0f)
        stage.batch.end()
    }
}

class SpriteView(val assets: Assets) : Table() {

    var selectedSprite: Sprite? = null

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        val sprite = selectedSprite ?: return
        val texture = assets.get<Texture>(sprite.texture)

        batch.draw(
            /* region = */ TextureRegion(texture),
            /* x = */ width / 2f - sprite.offsetX,
            /* y = */ height / 2f - sprite.offsetY,
            /* originX = */ 0f,
            /* originY = */ 0f,
            /* width = */ texture.width.toFloat(),
            /* height = */ texture.height.toFloat(),
            /* scaleX = */ 5f,
            /* scaleY = */ 5f,
            /* rotation = */ 0f
        )
    }
}
