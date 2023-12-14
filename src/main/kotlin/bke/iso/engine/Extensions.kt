package bke.iso.engine

// TODO: move to editor package for now
inline fun <reified T : Any> Collection<*>.withFirstInstance(action: (T) -> Unit) =
    filterIsInstance<T>()
        .firstOrNull()
        ?.let { instance -> action.invoke(instance) }
