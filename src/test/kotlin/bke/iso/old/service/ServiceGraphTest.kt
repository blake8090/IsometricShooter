package bke.iso.old.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ServiceGraphTest {

    @Test
    fun `when add, given duplicate, then throw exception`() {
        class A : TransientService

        val graph = ServiceGraph()
        graph.add<A>()

        assertThrows<DuplicateServiceException> {
            graph.add<A>()
        }
    }

    @Test
    fun `when get, given service not in graph, then throw exception`() {
        class A : TransientService
        class B : TransientService

        val graph = ServiceGraph()
        graph.add<A>()

        assertThrows<ServiceNotFoundException> {
            graph.get(B::class)
        }
    }

    @Test
    fun `when contains, then return`() {
        class A : TransientService
        class B : TransientService

        val graph = ServiceGraph()
        graph.add<A>()

        assertThat(graph.contains(A::class)).isTrue
        assertThat(graph.contains(B::class)).isFalse
    }

    @Test
    fun `when getNodes, then return`() {
        class A : TransientService
        class B : TransientService

        val graph = ServiceGraph()
        graph.add<A>()
        graph.add<B>()

        val a = graph.get(A::class)
        val b = graph.get(B::class)
        assertThat(graph.getNodes()).containsExactlyInAnyOrder(a, b)
    }

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
