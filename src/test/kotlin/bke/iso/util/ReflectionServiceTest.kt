package bke.iso.util

import bke.iso.util.test.ExampleClass
import bke.iso.util.test.ExampleAnnotation
import bke.iso.util.test.ValidClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReflectionServiceTest {
    @Test
    fun `should return all sub types with annotation`() {
        val results =
            ReflectionService().findSubTypesWithAnnotation(
                "bke.iso.util.test",
                ExampleAnnotation::class,
                ExampleClass::class
            )
        assertThat(results).containsExactlyInAnyOrder(ValidClass::class)
    }
}
