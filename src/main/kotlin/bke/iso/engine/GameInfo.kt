package bke.iso.engine

abstract class GameInfo {
    abstract val windowTitle: String

    abstract fun start(game: Game)
}
