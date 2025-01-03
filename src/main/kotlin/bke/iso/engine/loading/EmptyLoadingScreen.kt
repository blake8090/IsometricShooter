package bke.iso.engine.loading

class EmptyLoadingScreen : LoadingScreen() {

    override val transitionInState: TransitionInState = object : TransitionInState() {
        override fun start() {
            nextState()
        }

        override fun update(deltaTime: Float) {}
    }

    override val loadingState: LoadingState = object : LoadingState() {
        override fun start() {}

        override fun update(deltaTime: Float) {}
    }

    override val transitionOutState: TransitionOutState = object : TransitionOutState() {
        override fun start() {
            nextState()
        }

        override fun update(deltaTime: Float) {}
    }
}
