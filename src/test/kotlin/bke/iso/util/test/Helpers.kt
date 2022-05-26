package bke.iso.util.test

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ExampleAnnotation

internal abstract class ExampleClass

@ExampleAnnotation
internal class ValidClass : ExampleClass()

internal class ClassWithoutAnnotation : ExampleClass()

internal open class A
internal class ClassOfDifferentSubType : A()
