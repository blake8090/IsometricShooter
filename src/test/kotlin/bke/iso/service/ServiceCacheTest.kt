package bke.iso.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ServiceCacheTest {
    @Test
    fun whenInitialize_thenCallPostInit() {
        class Service {
            var value = 5

            @PostInit
            fun setup() {
                value = 2
            }
        }

        val cache = ServiceCache()
        cache[Service::class] = ServiceRecord(Service::class, Service::class, Lifetime.SINGLETON, listOf())
        cache.initialize()

        val service = cache[Service::class]
        assertThat(service.value).isEqualTo(2)
    }
}
