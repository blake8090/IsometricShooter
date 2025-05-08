package bke.iso.engine.profiler

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.core.Event
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.scene2d.Scene2dView
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.Event as Scene2dEvent

class ProfilerView(assets: Assets) : Scene2dView(assets) {

    private lateinit var label: Label


    override fun create() {
        setup()

        val table = Table().top().left()
        table.setFillParent(true)
        stage.addActor(table)

        label = Label("", skin)

        table.add(label)
            .padLeft(5f)
            .padTop(5f)
    }

    override fun handleScene2dEvent(event: Scene2dEvent) {
    }

    override fun handleEvent(event: Event) {
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 16f, Color.WHITE)])
        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
        })
    }

    fun setText(text: String) {
        if (this::label.isInitialized) {
            label.setText(text)
        }
    }
}