package bke.iso.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

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
