package bke.iso.engine.profiler

import bke.iso.engine.asset.Assets
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.input.keymouse.KeyBinding
import bke.iso.engine.ui.UI
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.ObjectFloatMap
import kotlin.time.measureTime
import com.badlogic.gdx.Input as GdxInput

private const val UPDATE_FREQUENCY_SECONDS = 0.25f

class Profiler(
    assets: Assets,
    private val ui: UI,
    private val input: Input
) {

    val records = ObjectFloatMap<String>()

    private val stringBuilder = StringBuilder()
    private var timer = 0f
    private var data: String = ""

    private val profilerScreen = ProfilerScreen(assets)

    fun start() {
        input.keyMouse.bind("profiler-modifier" to KeyBinding(GdxInput.Keys.SHIFT_LEFT, ButtonState.DOWN))
        input.keyMouse.bind("profiler-enable" to KeyBinding(GdxInput.Keys.P, ButtonState.PRESSED))
    }

    inline fun profile(name: String, action: () -> Unit) {
        val timeMicroseconds = measureTime(action).inWholeNanoseconds / 1000f
        records.put(name, timeMicroseconds)
    }

    fun update(deltaTime: Float) {
        timer -= deltaTime
        if (timer <= 0f) {
            data = getData()
            timer = UPDATE_FREQUENCY_SECONDS
        }

        if (input.poll("profiler-modifier") != 0f && input.poll("profiler-enable") != 0f) {
            ui.addScreen(profilerScreen)
        }

        profilerScreen.setText("${Gdx.graphics.framesPerSecond} FPS\n$data")
    }

    private fun getData(): String {
        stringBuilder.clear()
        for (record in records) {
            val name = record.key
            val value = record.value
            stringBuilder.append("$name - $value mcs")
            stringBuilder.append("\n")
        }
        return stringBuilder.toString()
    }
}