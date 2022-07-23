package bke.iso.engine.di.test

import bke.iso.engine.di.Singleton
import bke.iso.engine.di.SingletonImpl

@Singleton
internal class ExampleSingletonService

internal interface Shape {
    fun getType(): String
}

@SingletonImpl(Shape::class)
internal class Rectangle : Shape {
    override fun getType() = "rectangle"
}
