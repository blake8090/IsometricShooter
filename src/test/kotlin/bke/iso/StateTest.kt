package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.system.System
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.reflect.KClass

internal class StateTest {
    @Test
    fun `should update systems`() {
        class SystemA : System() {
            var initialized = false
            var updated = false

            override fun init() {
                initialized = true
            }

            override fun update(deltaTime: Float) {
                updated = true
            }
        }

        class SystemB : System() {
            var initialized = false
            var updated = false

            override fun init() {
                initialized = true
            }

            override fun update(deltaTime: Float) {
                updated = true
            }
        }

        class TestState : State() {
            override fun getSystems(): Set<KClass<out System>> =
                setOf(SystemA::class, SystemB::class)
        }

        val container = mock(ServiceContainer::class.java)
        val systemA = SystemA()
        val systemB = SystemB()
        `when`(container.createInstance(SystemA::class)).thenReturn(systemA)
        `when`(container.createInstance(SystemB::class)).thenReturn(systemB)

        val state = TestState()
        state.setup(container)
        state.updateSystems(0f)

        assertThat(systemA.initialized).isTrue
        assertThat(systemA.updated).isTrue
        assertThat(systemB.initialized).isTrue
        assertThat(systemB.updated).isTrue
    }
}
