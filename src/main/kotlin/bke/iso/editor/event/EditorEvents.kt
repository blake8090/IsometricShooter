package bke.iso.editor.event

import com.badlogic.gdx.scenes.scene2d.Event

open class EditorEvent : Event()

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()
