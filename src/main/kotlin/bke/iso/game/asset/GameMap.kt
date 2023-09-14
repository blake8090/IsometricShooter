package bke.iso.game.asset

class GameMap(val layers: List<Layer>) {
    class Layer(val z: Int) {
        val tiles: MutableList<String> = mutableListOf()
        val entities: MutableList<String> = mutableListOf()
    }
}
