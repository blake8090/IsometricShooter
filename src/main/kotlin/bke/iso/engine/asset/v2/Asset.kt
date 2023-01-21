package bke.iso.engine.asset.v2

data class Asset<T>(
    val name: String,
    val path: String,
    val extension: String,
    val value: T
)
