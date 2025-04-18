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
import imgui.flag.ImGuiWindowFlags
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

    private val log = KotlinLogging.logger { }

    private val inputTextLength = 50

    private var selectedIndex = -1
    private var showDemoWindow = false

    private var openSelectComponentPopup = false

    fun draw(viewData: ActorMode.ViewData) {
        beginImGuiFrame()
        drawMainMenuBar()
        drawComponentList(viewData)
        drawComponentEditor(viewData)
        drawSelectComponentPopup(viewData)

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
                    events.fire(ActorMode.SaveClicked())
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

    private fun drawComponentList(viewData: ActorMode.ViewData) {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Components")

        if (ImGui.button("Add")) {
            openSelectComponentPopup = true
        }
        ImGui.sameLine()
        if (ImGui.button("Delete")) {
            events.fire(ActorMode.SelectedComponentDeleted())
            selectedIndex = -1
        }

        if (ImGui.beginListBox("##components", size.x, 10 * ImGui.getTextLineHeightWithSpacing())) {
            for ((index, component) in viewData.components.withIndex()) {
                var selected = false
                if (viewData.selectedComponent == component) {
                    selectedIndex = index
                    selected = true
                }

                if (ImGui.selectable("${component::class.simpleName}", selected)) {
                    events.fire(ActorMode.ComponentSelected(component))
                }
            }
            ImGui.endListBox()
        }

        ImGui.end()
    }

    private fun drawSelectComponentPopup(viewData: ActorMode.ViewData) {
        if (openSelectComponentPopup) {
            ImGui.openPopup("Component Types##selectComponentPopup")
            openSelectComponentPopup = false
        }

        if (ImGui.beginPopupModal("Component Types##selectComponentPopup", null, ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Select a component type to add.")
            if (ImGui.beginListBox("##components")) {
                for (componentType in viewData.componentTypes.sortedBy { type -> type.simpleName }) {
                    if (ImGui.selectable("${componentType.simpleName}", false)) {
                        events.fire(ActorMode.NewComponentTypeAdded(componentType))
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endListBox()
            }

            if (ImGui.button("Cancel")) {
                ImGui.closeCurrentPopup()
            }

            ImGui.endPopup()
        }
    }

    private fun drawComponentEditor(viewData: ActorMode.ViewData) {
        val size = ImVec2(ImGui.getMainViewport().workSize)
        size.x *= 0.15f
        val pos = ImVec2(ImGui.getMainViewport().workPos)
        pos.x = ImGui.getMainViewport().workSizeX - size.x

        ImGui.setNextWindowPos(pos)
        ImGui.setNextWindowSize(size)
        ImGui.begin("Component Editor")
        viewData.selectedComponent?.let(::drawControls)
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
                if (xValue.get() != vector.x) {
                    val command = UpdateVector3Command(vector, xValue.get(), vector.y, vector.z)
                    events.fire(ImGuiEditorState.ExecuteCommand(command))
                }
            }

            val yValue = ImFloat(vector.y)
            if (ImGui.inputFloat("y##${memberProperty.name}", yValue)) {
                if (yValue.get() != vector.y) {
                    val command = UpdateVector3Command(vector, vector.x, yValue.get(), vector.z)
                    events.fire(ImGuiEditorState.ExecuteCommand(command))
                }
            }

            val zValue = ImFloat(vector.z)
            if (ImGui.inputFloat("z##${memberProperty.name}", zValue)) {
                if (zValue.get() != vector.z) {
                    val command = UpdateVector3Command(vector, xValue.get(), vector.y, zValue.get())
                    events.fire(ImGuiEditorState.ExecuteCommand(command))
                }
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
