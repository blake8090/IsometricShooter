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
        for (shaderInfo in assets.getAll<ShaderInfo>()) {
            compile(shaderInfo)
        }
    }

    private fun compile(shaderInfo: ShaderInfo) {
        val shaderName = shaderInfo.name
        val vertexShader = assets.get<ShaderFile>(shaderInfo.vertexShader)
        val fragShader = assets.get<ShaderFile>(shaderInfo.fragmentShader)

        val program = ShaderProgram(vertexShader.content, fragShader.content)
        check(program.isCompiled) {
            "Shader program '$shaderName' was not compiled:\n${program.log}"
        }

        programsByName[shaderName] = program
        log.info { "Compiled shader '$shaderName' from vert: '${vertexShader.fileName}' frag: '${fragShader.fileName}'" }
    }
}
