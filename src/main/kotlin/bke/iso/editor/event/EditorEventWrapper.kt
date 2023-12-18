package bke.iso.editor.event

import bke.iso.engine.Event

data class EditorEventWrapper(val editorEvent: EditorEvent) : Event
