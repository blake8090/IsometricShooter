package bke.iso.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class ProviderTest {
    @Test
    fun whenGet_thenCallGetBaseClass() {
        class A

        val container = mock<ServiceContainer>()
        val provider = Provider(container, A::class)
        provider.get()

        verify(container, only()).get<A>()
    }

    @Test
    fun whenGetSubClass_thenCallGetSubClass() {
        open class A
        class B : A()

        val container = mock<ServiceContainer>()
        val provider = Provider(container, A::class)
        provider.get(B::class)

        verify(container, only()).get<B>()
    }
}
