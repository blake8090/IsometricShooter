package bke.iso.service

import bke.iso.service.cache.ServiceCache
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.only
import org.mockito.Mockito.verify

internal class ProviderTest {
    @Test
    fun whenGet_thenCallGetBaseClass() {
        class A

        val cache = mock<ServiceCache>()
        val provider = Provider(cache, A::class)
        provider.get()

        verify(cache, only())[A::class]
    }

    @Test
    fun whenGetSubClass_thenCallGetSubClass() {
        open class A
        class B : A()

        val cache = mock<ServiceCache>()
        val provider = Provider(cache, A::class)
        provider.get(B::class)

        verify(cache, only())[B::class]
    }
}
