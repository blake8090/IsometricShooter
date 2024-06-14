package bke.iso.engine.asset.shader

import bke.iso.engine.asset.Assets
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import mu.KotlinLogging

class Shaders(private val assets: Assets) {
    private val log = KotlinLogging.logger {}

    private val programsByName = mutableMapOf<String, ShaderProgram>()

    init {
        ShaderProgram.pedantic = false
    }

    operator fun get(name: String): ShaderProgram? =
        programsByName[name]

    fun compileAll() {
        val filesByName = assets.getAll<ShaderFile>()
            .groupBy { getShaderName(it) }

        for ((shaderName, files) in filesByName) {
            val program = getShaderProgram(shaderName, files)
            check(program.isCompiled) {
                "Shader program '$shaderName' was not compiled:\n${program.log}"
            }

            programsByName[shaderName] = program
            log.info { "Compiled shader '$shaderName' from ${files.map(ShaderFile::fileName)}" }
        }
    }

    private fun getShaderProgram(shaderName: String, files: List<ShaderFile>): ShaderProgram {
        check(files.size == 2) {
            "Expected only 2 files for shader '$shaderName'"
        }

        val vertShaderFile = checkNotNull(files.find { file -> file.fileName == "$shaderName.vert.glsl" }) {
            "Missing vertex shaders for '$shaderName'"
        }

        val fragShaderFile = checkNotNull(files.find { file -> file.fileName == "$shaderName.frag.glsl" }) {
            "Missing frag shaders for '$shaderName'"
        }

        return ShaderProgram(vertShaderFile.content, fragShaderFile.content)
    }

    /**
     * Parses the shader name from the filename, assuming the format "{name}.{frag/vert}.glsl"
     */
    private fun getShaderName(shaderFile: ShaderFile): String {
        return shaderFile.fileName.split('.', limit = 2)
            .firstOrNull()
            ?: throw IllegalArgumentException("filename ${shaderFile.fileName} not formatted properly")
    }
}
