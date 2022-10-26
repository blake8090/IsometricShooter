package bke.iso.v2.engine

abstract class State {
    open fun start() {}
    open fun update(deltaTime: Float) {}
    open fun stop() {}
}

class EmptyState : State()
