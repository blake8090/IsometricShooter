package bke.iso.util

import bke.iso.util.test.ExampleAnnotation
import bke.iso.util.test.ExampleClass
import bke.iso.util.test.ValidClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val basePackage = "bke.iso.util.test"

internal class ReflectionServiceTest {
    @Test
    fun `should return types with annotation`() {
        val results = ReflectionService().findTypesWithAnnotation<ExampleAnnotation>(basePackage)
        assertThat(results).containsExactlyInAnyOrder(ValidClass::class)
    }

    @Test
    fun `should return all sub types with annotation`() {
        val results =
            ReflectionService().findSubTypesWithAnnotation<ExampleClass, ExampleAnnotation>(basePackage)
        assertThat(results).containsExactlyInAnyOrder(ValidClass::class)
    }
}
