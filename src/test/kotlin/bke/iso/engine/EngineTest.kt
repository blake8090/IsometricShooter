package bke.iso.engine

import bke.iso.engine.di.ServiceContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class EngineTest {
    @Test
    fun `when changing state, should setup and start the new state`() {
        class TestState : State()

        val container = mock(ServiceContainer::class.java)
        val state = mock(TestState::class.java)
        `when`(container.createInstance(TestState::class)).thenReturn(state)

        val engine = Engine(container)
        engine.changeState(TestState::class)

        verify(state, times(1)).setup(container)
        verify(state, times(1)).start()
    }

    @Test
    fun `when changing state, should stop old state`() {
        class TestState : State()
        class TestState2 : State()

        val container = mock(ServiceContainer::class.java)
        val state = mock(TestState::class.java)
        val state2 = mock(TestState2::class.java)
        `when`(container.createInstance(TestState::class)).thenReturn(state)
        `when`(container.createInstance(TestState2::class)).thenReturn(state2)

        val engine = Engine(container)
        engine.changeState(TestState::class)
        engine.changeState(TestState2::class)

        verify(state, times(1)).setup(container)
        verify(state, times(1)).start()
        verify(state, times(1)).stop()

        verify(state2, times(1)).setup(container)
        verify(state2, times(1)).start()
    }

    @Test
    fun `when emitting events, should run handlers in the following order - (state, engine)`() {
        class EventA : Event()

        val numbers = mutableListOf<Int>()

        class TestState : State() {
            override fun start() {
                eventHandlers.handleEvent<EventA> { numbers.add(1) }
            }
        }

        val container = mock(ServiceContainer::class.java)
        `when`(container.createInstance(TestState::class)).thenReturn(TestState())

        val engine = Engine(container)
        engine.eventHandlers.handleEvent<EventA> { numbers.add(2) }
        engine.changeState(TestState::class)
        engine.emitEvent(this, EventA())

        assertThat(numbers).containsExactly(1, 2)
    }
}
