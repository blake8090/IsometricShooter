package bke.iso.engine

abstract class GameInfo {
    abstract val windowTitle: String

    /**
     * The name of the folder where game data is saved.
     */
    abstract val folderName: String

    abstract fun start(engine: Engine)
}
