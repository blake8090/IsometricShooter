package bke.iso.engine.util

import bke.iso.engine.util.test.ExampleAnnotation
import bke.iso.engine.util.test.ExampleClass
import bke.iso.engine.util.test.ValidClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val basePackage = "bke.iso.engine.util.test"

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
