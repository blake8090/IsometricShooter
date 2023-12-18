package bke.iso.editor.event

import com.badlogic.gdx.scenes.scene2d.Event

open class EditorEvent : Event()

class SaveSceneEvent : EditorEvent()

class OpenSceneEvent : EditorEvent()

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()
