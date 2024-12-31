package bke.iso.engine.os

import bke.iso.engine.asset.BASE_PATH
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NativeFileDialog
import java.io.File
import kotlin.io.path.Path

/**
 * Provides an interface for calling native OS dialogs.
 */
class Dialogs {

    private val log = KotlinLogging.logger {}

    /**
     * Shows a native open file dialog, filtered to the given [extension].
     *
     * @param description the description of the file - i.e. `"Zip file"`
     * @param extension the file's extension (without the dot) - i.e. `"zip"`
     * @return a file if chosen, null otherwise
     */
    fun showOpenFileDialog(description: String, extension: String): File? {
        var file: File? = null

        MemoryStack.stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8(description))
                .spec(stack.UTF8(extension))

            val pathPointer = stack.mallocPointer(1)
            val defaultPath = Path(BASE_PATH)
                .toAbsolutePath()
                .toString()
            val result = NativeFileDialog.NFD_OpenDialog(pathPointer, filters, defaultPath)

            when (result) {
                NativeFileDialog.NFD_OKAY -> {
                    file = File(pathPointer.getStringUTF8(0))
                    NativeFileDialog.NFD_FreePath(pathPointer.get(0))
                }

                NativeFileDialog.NFD_CANCEL -> {
                    log.debug { "User cancelled open" }
                }

                NativeFileDialog.NFD_ERROR -> {
                    error("Error in NFD_OpenDialog: ${NativeFileDialog.NFD_GetError()}")
                }
            }
        }

        return file
    }

    /**
     * Shows a native save file dialog, filtered to the given [extension].
     *
     * @param description the description of the file - i.e. `"Zip file"`
     * @param extension the file's extension (without the dot) - i.e. `"zip"`
     * @return a file if chosen, null otherwise
     */
    fun showSaveFileDialog(description: String, extension: String): File? {
        var file: File? = null

        MemoryStack.stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8(description))
                .spec(stack.UTF8(extension))

            val pathPointer = stack.mallocPointer(1)
            // TODO: investigate why default path setting does not work
            val defaultPath = Path(BASE_PATH)
                .toAbsolutePath()
                .toString()
            val result = NativeFileDialog.NFD_SaveDialog(pathPointer, filters, defaultPath, "untitled")

            when (result) {
                NativeFileDialog.NFD_OKAY -> {
                    file = File(pathPointer.getStringUTF8(0))
                    NativeFileDialog.NFD_FreePath(pathPointer.get(0))
                }

                NativeFileDialog.NFD_CANCEL -> {
                    log.debug { "User cancelled save" }
                }

                NativeFileDialog.NFD_ERROR -> {
                    error("Error in NFD_SaveDialog: ${NativeFileDialog.NFD_GetError()}")
                }
            }
        }

        return file
    }
}
