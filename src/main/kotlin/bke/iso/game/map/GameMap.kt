package bke.iso.game.map

class GameMap(val layers: List<Layer>) {
    class Layer(val z: Int) {
        val tiles = mutableListOf<String>()
        val entities = mutableListOf<String>()
    }
}
