package bke.iso.editor2.actor

import bke.iso.editor2.ImGuiEditorState
import bke.iso.editor2.actor.command.UpdateComponentPropertyCommand
import bke.iso.editor2.actor.command.UpdateVector3Command
import bke.iso.engine.asset.Assets
import bke.iso.engine.beginImGuiFrame
import bke.iso.engine.core.Events
import bke.iso.engine.endImGuiFrame
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector3
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImInt
import imgui.type.ImString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.cast
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ActorModeView(
    private val events: Events,
    private val assets: Assets,
) {

    private val inputTextLength = 50

    private val log = KotlinLogging.logger { }

    private var selectedIndex = -1
    private var selectedComponent: Component? = null

    private var showDemoWindow = false

    fun reset() {
        selectedIndex = -1
        selectedComponent = null
    }

    fun draw(components: List<Component>) {
        beginImGuiFrame()
        drawMainMenuBar()
        drawComponentList(components)
        drawComponentEditor()

        if (showDemoWindow) {
            ImGui.showDemoWindow()
        }

        endImGuiFrame()
    }

    private fun drawMainMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("New")

                if (ImGui.menuItem("Open", "Ctrl+O")) {
                    events.fire(ActorMode.OpenClicked())
                }

                if (ImGui.menuItem("Save", "Ctrl+S")) {
                }

                ImGui.menuItem("Save as..")
                ImGui.menuItem("Exit")
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Mode")) {
                if (ImGui.menuItem("Scene Editor", false)) {
                    events.fire(ImGuiEditorState.SceneModeSelected())
                }
                ImGui.menuItem("Actor Editor", true)
                ImGui.beginDisabled()
                ImGui.menuItem("Particle Editor", false)
                ImGui.menuItem("Animation Editor", false)
                ImGui.menuItem("Weapon Editor", false)
                ImGui.endDisabled()
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Debug")) {
                if (ImGui.menuItem("Show Demo Window", showDemoWindow)) {
                    showDemoWindow = !showDemoWindow
                }
                ImGui.endMenu()
            }

            ImGui.endMainMenuBar()
        }
    }

    private fun drawComponentList(components: List<Component>) {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Components")

        ImGui.button("Add")
        ImGui.sameLine()
        ImGui.button("Delete")

        if (ImGui.beginListBox("##components", size.x, 5 * ImGui.getTextLineHeightWithSpacing())) {
            for ((index, component) in components.withIndex()) {
                val selected = selectedIndex == index
                if (ImGui.selectable("${component::class.simpleName}", selected)) {
                    selectedIndex = index
                    selectedComponent = component
                }
            }
            ImGui.endListBox()
        }

        ImGui.end()
    }

    private fun drawComponentEditor() {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)
        pos.x = ImGui.getMainViewport().workSizeX - size.x

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Component Editor")
        selectedComponent?.let(::drawControls)
        ImGui.end()
    }

    private fun drawControls(component: Component) {
        for (memberProperty in component::class.memberProperties) {
            when (memberProperty.returnType) {
                typeOf<Float>() -> drawFloatControls(component, memberProperty)
                typeOf<Float?>() -> drawFloatControls(component, memberProperty)
                typeOf<Int>() -> drawIntControls(component, memberProperty)
                typeOf<Boolean>() -> drawBooleanControls(component, memberProperty)
                typeOf<String>() -> drawStringControls(component, memberProperty)
                typeOf<Vector3>() -> drawVector3Controls(component, memberProperty)
                typeOf<Color>() -> drawColorControls(component, memberProperty)
                else -> log.warn { "Could not generate controls for component ${component::class.simpleName} - KType ${memberProperty.returnType}" }
            }
        }
    }

    private fun drawStringControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val isMutable = memberProperty is KMutableProperty<*>

        if (!isMutable) {
            ImGui.beginDisabled()
        }

        val value = ImString(memberProperty.getter.call(component).toString(), inputTextLength)
        if (ImGui.inputText(memberProperty.name, value)) {
            if (memberProperty is KMutableProperty<*>) {
                if (memberProperty.name != "texture" || assets.contains(value.toString(), Texture::class)) {
                    events.fire(
                        ImGuiEditorState.ExecuteCommand(
                            UpdateComponentPropertyCommand(
                                component = component,
                                property = memberProperty as KMutableProperty1,
                                newValue = value.toString()
                            )
                        )
                    )
                }
            }
        }

        if (!isMutable) {
            ImGui.endDisabled()
        }
    }

    private fun drawFloatControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImFloat(memberProperty.getter.call(component) as Float)
        if (ImGui.inputFloat(memberProperty.name, value)) {
            val command =
                UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(ImGuiEditorState.ExecuteCommand(command))
        }
    }

    private fun drawBooleanControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImBoolean(memberProperty.getter.call(component) as Boolean)
        if (ImGui.checkbox(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(ImGuiEditorState.ExecuteCommand(command))
        }
    }

    private fun drawIntControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        val value = ImInt(memberProperty.getter.call(component) as Int)
        if (ImGui.inputInt(memberProperty.name, value)) {
            val command = UpdateComponentPropertyCommand(component, memberProperty as KMutableProperty1, value.get())
            events.fire(ImGuiEditorState.ExecuteCommand(command))
        }
    }

    private fun drawVector3Controls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.collapsingHeader(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val vector = Vector3::class.cast(memberProperty.getter.call(component))

            val xValue = ImFloat(vector.x)
            if (ImGui.inputFloat("x##${memberProperty.name}", xValue)) {
                val command = UpdateVector3Command(vector, xValue.get(), vector.y, vector.z)
                events.fire(ImGuiEditorState.ExecuteCommand(command))
            }

            val yValue = ImFloat(vector.z)
            if (ImGui.inputFloat("y##${memberProperty.name}", yValue)) {
                val command = UpdateVector3Command(vector, vector.x, yValue.get(), vector.z)
                events.fire(ImGuiEditorState.ExecuteCommand(command))
            }

            val zValue = ImFloat(vector.z)
            if (ImGui.inputFloat("z##${memberProperty.name}", zValue)) {
                val command = UpdateVector3Command(vector, xValue.get(), vector.y, zValue.get())
                events.fire(ImGuiEditorState.ExecuteCommand(command))
            }
        }
    }

    private fun drawColorControls(component: Component, memberProperty: KProperty1<out Component, *>) {
        if (ImGui.collapsingHeader(memberProperty.name, ImGuiTreeNodeFlags.DefaultOpen)) {
            val color = Color::class.cast(memberProperty.getter.call(component))
            ImGui.inputFloat("r##${memberProperty.name}", ImFloat(color.r))
            ImGui.inputFloat("g##${memberProperty.name}", ImFloat(color.g))
            ImGui.inputFloat("b##${memberProperty.name}", ImFloat(color.b))
            ImGui.inputFloat("a##${memberProperty.name}", ImFloat(color.a))
        }
    }
}
