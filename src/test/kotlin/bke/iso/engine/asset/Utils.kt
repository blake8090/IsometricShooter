package bke.iso.engine.asset

import bke.iso.engine.FilePointer
import io.mockk.every
import io.mockk.mockk

internal fun mockFilePointer(path: String, name: String, extension: String): FilePointer =
    mockk<FilePointer>().apply {
        every { getPath() } returns path
        every { getNameWithoutExtension() } returns name
        every { getExtension() } returns extension
    }
