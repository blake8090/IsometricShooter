package bke.iso.service

import bke.iso.service.container.ServiceContainer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.only
import org.mockito.Mockito.verify

internal class ProviderTest {
    @Test
    fun whenGet_thenCallGetBaseClass() {
        class A

        val cache = mock<ServiceContainer>()
        val provider = Provider(cache, A::class)
        provider.get()

        verify(cache, only()).get(A::class)
    }

    @Test
    fun whenGetSubClass_thenCallGetSubClass() {
        open class A
        class B : A()

        val cache = mock<ServiceContainer>()
        val provider = Provider(cache, A::class)
        provider.get(B::class)

        verify(cache, only()).get(B::class)
    }
}
