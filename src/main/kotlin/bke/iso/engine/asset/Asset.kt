package bke.iso.engine.asset

data class Asset<T>(
    val name: String,
    val path: String,
    val extension: String,
    val value: T
)
