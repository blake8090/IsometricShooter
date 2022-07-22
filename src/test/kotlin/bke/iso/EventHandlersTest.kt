package bke.iso

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

internal class EventHandlersTest {
    @Test
    fun `should register and run event handlers`() {
        class EventA : Event()
        class EventB : Event()
        class EventC : Event()

        val eventHandlers = EventHandlers()

        var numEventA = 0
        eventHandlers.handleEvent<EventA> {
            numEventA++
        }

        var numEventB = 0
        eventHandlers.handleEvent<EventB> {
            numEventB++
        }

        var numEventC = 0
        eventHandlers.handleEvent<EventC> {
            numEventC++
        }

        eventHandlers.run(EventA())
        eventHandlers.run(EventA())
        eventHandlers.run(EventB())

        assertThat(numEventA).isEqualTo(2)
        assertThat(numEventB).isEqualTo(1)
        assertThat(numEventC).isEqualTo(0)
    }

    @Test
    fun `should run multiple event handlers for a single event`() {
        class EventA : Event()

        val eventHandlers = EventHandlers()

        val numbers = mutableListOf<Int>()
        eventHandlers.handleEvent<EventA> {
            numbers.add(1)
        }
        eventHandlers.handleEvent<EventA> {
            numbers.add(2)
        }

        eventHandlers.run(EventA())
        assertThat(numbers).containsExactly(1, 2)
    }

    @Test
    fun `should not throw exceptions when an event has no event handlers`() {
        class EventA : Event()

        val eventHandlers = EventHandlers()

        val throwable = catchThrowable { eventHandlers.run(EventA()) }
        assertThat(throwable).isNull()
    }

    @Test
    fun `should not use event handler for base Event type`() {
        class EventA : Event()

        val eventHandlers = EventHandlers()

        val numbers = mutableListOf<Int>()
        eventHandlers.handleEvent<Event> {
            numbers.add(1)
        }
        eventHandlers.handleEvent<EventA> {
            numbers.add(2)
        }

        eventHandlers.run(EventA())
        assertThat(numbers).containsExactly(2)
    }
}
