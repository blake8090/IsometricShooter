package bke.iso.service.v2

import bke.iso.service.ServiceGraph
import bke.iso.service.TransientService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

// TODO: finish tests
class ServiceGraphTest {

    @Test
    fun `when link, given cycle, then throw exception`() {
        class A : TransientService
        class B : TransientService

        val graph = ServiceGraph()
        graph.add<A>()
        graph.add<B>()
        graph.link<A, B>()

        assertThrows<Error> {
            graph.link<B, A>()
        }

    }

    /**
     * Given:
     * - A -> B -> D -> E
     * - C -> D -> E
     *
     * When: link E to A
     *
     * Then: throw exception
     */
    @Test
    fun `when link, given backwards cycle, then throw exception`() {
        class A : TransientService
        class B : TransientService
        class C : TransientService
        class D : TransientService
        class E : TransientService

        val graph = ServiceGraph()
        graph.add<A>()
        graph.add<B>()
        graph.add<C>()
        graph.add<D>()
        graph.add<E>()

        graph.link<A, B>()
        graph.link<B, D>()
        graph.link<C, D>()
        graph.link<D, E>()

        assertThrows<Error> {
            graph.link<E, A>()
        }
    }

    /**
     * Given:
     * - A -> B -> C
     * - D -> E -> A
     *
     * When: link C to D
     *
     * Then: throw exception
     */
    @Test
    fun `when link, given forward cycle, then throw exception`() {
        class A : TransientService
        class B : TransientService
        class C : TransientService
        class D : TransientService
        class E : TransientService

        val graph = ServiceGraph()
        graph.add<A>()
        graph.add<B>()
        graph.add<C>()
        graph.add<D>()
        graph.add<E>()

        graph.link<A, B>()
        graph.link<B, C>()
        graph.link<D, E>()
        graph.link<E, A>()

        assertThrows<Error> {
            graph.link<C, D>()
        }
    }
}
