package bke.iso.v2.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ServiceCacheTest {
    @Test
    fun givenServiceType_shouldReturnSameInstance() {
        class A(val id: Int)

        val serviceCache = ServiceCache()
        serviceCache.add(A::class) { A(50) }

        val service = serviceCache.get(A::class)
        assertThat(service.id).isEqualTo(50)

        val service2 = serviceCache.get(A::class)
        assertThat(service2.id).isEqualTo(50)
        assertThat(service2).isSameAs(service)
    }

    @Test
    fun givenMissingServiceType_shouldThrowException() {
        class A

        val serviceCache = ServiceCache()

        assertThrows<MissingServiceException> { serviceCache.get(A::class) }
    }
}
