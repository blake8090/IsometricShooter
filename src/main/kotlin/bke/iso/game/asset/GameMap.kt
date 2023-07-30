package bke.iso.game.asset

class GameMap(val layers: List<Layer>) {
    class Layer(val z: Int) {
        val tiles = mutableListOf<String>()
        val entities = mutableListOf<String>()
    }
}
