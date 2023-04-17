package bke.iso.service.v2

import kotlin.reflect.KClass

data class Node<T : Service>(
    val type: KClass<T>,
    val parents: MutableSet<KClass<out Service>> = mutableSetOf(),
    val links: MutableSet<KClass<out Service>> = mutableSetOf(),
    var instance: T? = null
)

class ServiceGraph {

    private val nodes = mutableSetOf<Node<*>>()

    fun <T : Service> add(type: KClass<out T>): Node<out T> {
        val node = Node(type)
        if (!nodes.add(node)) {
            throw Error("already present")
        }
        return node
    }

    inline fun <reified T : Service> add() =
        add(T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Service> get(type: KClass<out T>): Node<out T> {
        val node = nodes.filter { node -> node.type == type }
            .firstOrNull()
            ?: throw Error("Service ${type.simpleName} was not found")
        return node as Node<out T>
    }

    inline fun <reified T : Service> get() =
        get(T::class)

    fun getNodes(): Set<Node<*>> =
        nodes

    fun <T : Service> contains(type: KClass<out T>) =
        nodes.any { node -> node.type == type }

    fun link(pair: Pair<KClass<out Service>, KClass<out Service>>) {
        val firstNode = get(pair.first)
        val secondNode = get(pair.second)
        validateLink(firstNode, secondNode, mutableSetOf())
        firstNode.links.add(secondNode.type)
        secondNode.parents.add(firstNode.type)
    }

    inline fun <reified Parent : Service, reified Link : Service> link() =
        link(Parent::class to Link::class)

    private fun validateLink(parent: Node<*>, link: Node<*>, chain: MutableSet<Node<*>>) {
        chain.add(parent)
        if (chain.contains(link)) {
            val cycle = chain.map(Node<*>::type)
                .map(KClass<*>::simpleName)
                .reversed()
                .joinToString(" -> ")
            throw Error("cycle found: $cycle -> [${link.type.simpleName}] ")
        }

        for (kClass in parent.parents) {
            val nextParent = get(kClass)
            val subChain = mutableSetOf<Node<*>>()
            subChain.addAll(chain)
            validateLink(nextParent, link, subChain)
        }
    }
}
