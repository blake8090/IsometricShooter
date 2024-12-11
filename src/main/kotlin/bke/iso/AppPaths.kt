package bke.iso

import bke.iso.engine.GameInfo
import org.apache.commons.lang3.SystemUtils

class AppPaths(private val gameInfo: GameInfo) {

    /**
     * Returns the user's home directory as a string.
     */
    fun getHomeDirectory(): String {
        return System.getProperty("user.home")
    }

    /**
     * Returns the path where game data such as saves, logs and configuration should be saved.
     * The returned path differs depending on the user's OS.
     */
    fun getGameDataPath(): String {
        return if (SystemUtils.IS_OS_WINDOWS) {
            "${System.getenv("LOCALAPPDATA")}\\${gameInfo.folderName}"
        } else {
            "${getHomeDirectory()}/${gameInfo.folderName}"
        }
    }
}
