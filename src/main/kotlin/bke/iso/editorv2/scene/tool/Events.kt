package bke.iso.editorv2.scene.tool

import bke.iso.editor.event.EditorEvent
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab

data class SelectTilePrefabEvent(val prefab: TilePrefab) : EditorEvent()

data class SelectActorPrefabEvent(val prefab: ActorPrefab) : EditorEvent()

class SelectPointerToolEvent : EditorEvent()

class SelectBrushToolEvent : EditorEvent()

class SelectEraserToolEvent : EditorEvent()

class SelectRoomToolEvent : EditorEvent()

class SelectFillToolEvent : EditorEvent()
