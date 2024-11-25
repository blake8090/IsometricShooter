package bke.iso.editor.scene.tool

import bke.iso.editor.EditorEvent
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.world.actor.Actor

data class SelectTilePrefabEvent(val prefab: TilePrefab) : EditorEvent()

data class SelectActorPrefabEvent(val prefab: ActorPrefab) : EditorEvent()

class SelectPointerToolEvent : EditorEvent()

class SelectBrushToolEvent : EditorEvent()

class SelectEraserToolEvent : EditorEvent()

class SelectRoomToolEvent : EditorEvent()

class SelectFillToolEvent : EditorEvent()

class PointerSelectActorEvent(val actor: Actor) : EditorEvent()

class PointerDeselectActorEvent : EditorEvent()
