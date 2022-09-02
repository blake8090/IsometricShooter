package bke.iso.v2.app

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ServicesTest {
    @Test
    fun whenCreated_shouldAddServicesClassToCache() {
        val services = Services()
        assertThat(services.get<Services>()).isSameAs(services)
    }

    @Test
    fun givenAServiceType_shouldReturnInstance() {
        class A

        val services = Services()
        services.register(A::class)

        assertDoesNotThrow {
            services.get<A>()
        }
    }

    @Test
    fun givenServiceType_shouldReturnInstanceWithDependencies() {
        class UserDatabase
        class UserFactory
        class UserService(
            val userDatabase: UserDatabase,
            val userFactory: UserFactory
        )

        class UserClient(val userService: UserService)

        val services = Services()
        services.register(UserService::class)
        services.register(UserFactory::class)
        services.register(UserDatabase::class)
        services.register(UserClient::class)

        assertDoesNotThrow {
            services.get<UserService>()
            services.get<UserClient>()
        }
    }
}
