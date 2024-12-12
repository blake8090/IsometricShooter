package bke.iso.engine

import org.apache.commons.lang3.SystemUtils

abstract class Game {

    abstract val windowTitle: String

    /**
     * The name of the folder where game data is saved.
     */
    abstract val folderName: String

    val userHomePath: String
        get() = System.getProperty("user.home")

    val gameDataPath: String
        get() =
            if (SystemUtils.IS_OS_WINDOWS) {
                "${System.getenv("LOCALAPPDATA")}\\$folderName"
            } else {
                "$userHomePath/$folderName"
            }

    abstract fun start(engine: Engine)
}
