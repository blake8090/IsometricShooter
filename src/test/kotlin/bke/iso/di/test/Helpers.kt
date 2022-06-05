package bke.iso.di.test

import bke.iso.di.Singleton
import bke.iso.di.SingletonImpl

@Singleton
internal class ExampleSingletonService

internal interface Shape {
    fun getType(): String
}

@SingletonImpl(Shape::class)
internal class Rectangle : Shape {
    override fun getType() = "rectangle"
}
