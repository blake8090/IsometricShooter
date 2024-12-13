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

    fun showOpenFileDialog(): File? {
        var file: File? = null

        MemoryStack.stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8("Scene"))
                .spec(stack.UTF8("scene"))

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

    fun showOpenActorDialog(): File? {
        var file: File? = null

        MemoryStack.stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8("Actor"))
                .spec(stack.UTF8("actor"))

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

    fun showSaveFileDialog(): File? {
        var file: File? = null

        MemoryStack.stackPush().use { stack ->
            val filters = NFDFilterItem.malloc(1)
            filters[0]
                .name(stack.UTF8("Scene"))
                .spec(stack.UTF8("scene"))

            val pathPointer = stack.mallocPointer(1)
            // TODO: investigate why default path setting does not work
            val defaultPath = Path(BASE_PATH)
                .toAbsolutePath()
                .toString()
            val result = NativeFileDialog.NFD_SaveDialog(pathPointer, filters, defaultPath, "untitled.scene")

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
